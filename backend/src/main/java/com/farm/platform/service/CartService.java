package com.farm.platform.service;

import com.farm.platform.dto.CartDto;
import com.farm.platform.dto.CartItemDto;
import com.farm.platform.entity.Product;
import com.farm.platform.entity.ProductStatus;
import com.farm.platform.entity.User;
import com.farm.platform.repository.ProductRepository;
import com.farm.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;

/**
 * 購物車：以 Redis Hash 儲存。
 * key:        cart:{userId}
 * hashKey:    productId (String)
 * hashValue:  quantity  (String)
 * TTL:        application.yml app.cart.ttl-seconds（預設 30 天）
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final StringRedisTemplate redis;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Value("${app.cart.ttl-seconds:2592000}")
    private long ttlSeconds;

    /* ============ 對外 API ============ */

    @Transactional(readOnly = true)
    public CartDto getCart(String email) {
        Long userId = userId(email);
        return buildDto(readRaw(userId));
    }

    @Transactional(readOnly = true)
    public CartDto addItem(String email, Long productId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("數量至少 1");
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (p.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalArgumentException("商品已下架或售完");
        }

        Long userId = userId(email);
        String key = key(userId);
        HashOperations<String, Object, Object> hash = redis.opsForHash();

        int existing = parseQty(hash.get(key, productId.toString()));
        int target = existing + quantity;
        if (target > p.getStock()) {
            throw new IllegalArgumentException("加入數量超過庫存（剩 " + p.getStock() + " " + p.getUnit() + "）");
        }
        hash.put(key, productId.toString(), String.valueOf(target));
        redis.expire(key, Duration.ofSeconds(ttlSeconds));

        return buildDto(readRaw(userId));
    }

    @Transactional(readOnly = true)
    public CartDto updateQuantity(String email, Long productId, int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("數量至少 1");
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (quantity > p.getStock()) {
            throw new IllegalArgumentException("數量超過庫存（剩 " + p.getStock() + " " + p.getUnit() + "）");
        }

        Long userId = userId(email);
        String key = key(userId);
        if (!Boolean.TRUE.equals(redis.opsForHash().hasKey(key, productId.toString()))) {
            throw new IllegalArgumentException("購物車內沒有此商品");
        }
        redis.opsForHash().put(key, productId.toString(), String.valueOf(quantity));
        redis.expire(key, Duration.ofSeconds(ttlSeconds));

        return buildDto(readRaw(userId));
    }

    @Transactional(readOnly = true)
    public CartDto removeItem(String email, Long productId) {
        Long userId = userId(email);
        String key = key(userId);
        redis.opsForHash().delete(key, productId.toString());
        return buildDto(readRaw(userId));
    }

    public void clear(String email) {
        Long userId = userId(email);
        redis.delete(key(userId));
    }

    /** 給結帳用：取 userId + 所有 productId→quantity */
    public Map<Long, Integer> getRawCart(Long userId) {
        return readRaw(userId);
    }

    /** 給結帳用：清空（不重新讀使用者） */
    public void clearByUserId(Long userId) {
        redis.delete(key(userId));
    }

    /* ============ 內部 ============ */

    private String key(Long userId) {
        return "cart:" + userId;
    }

    private Long userId(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("使用者不存在"));
        return u.getId();
    }

    private Map<Long, Integer> readRaw(Long userId) {
        Map<Object, Object> all = redis.opsForHash().entries(key(userId));
        Map<Long, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> e : all.entrySet()) {
            try {
                out.put(Long.parseLong(e.getKey().toString()), parseQty(e.getValue()));
            } catch (NumberFormatException ignored) {
                // 跳過壞資料
            }
        }
        return out;
    }

    private int parseQty(Object v) {
        if (v == null) return 0;
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /** 把 raw cart 轉成附帶商品快照的 DTO；同時把不存在/下架的商品標 available=false */
    private CartDto buildDto(Map<Long, Integer> raw) {
        if (raw.isEmpty()) {
            return CartDto.builder()
                    .items(List.of())
                    .totalQuantity(0)
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }
        List<Product> products = productRepository.findAllById(raw.keySet());
        Map<Long, Product> byId = new HashMap<>();
        for (Product p : products) byId.put(p.getId(), p);

        List<CartItemDto> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        for (Map.Entry<Long, Integer> e : raw.entrySet()) {
            Long pid = e.getKey();
            int qty = e.getValue();
            Product p = byId.get(pid);
            if (p == null) {
                // 商品被刪了，仍回傳一筆但 available=false
                items.add(CartItemDto.builder()
                        .productId(pid)
                        .name("（已下架商品）")
                        .quantity(qty)
                        .price(BigDecimal.ZERO)
                        .subtotal(BigDecimal.ZERO)
                        .stock(0)
                        .available(false)
                        .build());
                continue;
            }
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(qty));
            boolean available = p.getStatus() == ProductStatus.ACTIVE && p.getStock() >= qty;
            items.add(CartItemDto.builder()
                    .productId(pid)
                    .name(p.getName())
                    .imageUrl(p.getImageUrl())
                    .unit(p.getUnit())
                    .price(p.getPrice())
                    .stock(p.getStock())
                    .quantity(qty)
                    .subtotal(subtotal)
                    .available(available)
                    .farmerId(p.getFarmer().getId())
                    .farmerName(p.getFarmer().getName())
                    .build());
            if (available) {
                total = total.add(subtotal);
                count += qty;
            }
        }

        return CartDto.builder()
                .items(items)
                .totalQuantity(count)
                .totalAmount(total)
                .build();
    }
}
