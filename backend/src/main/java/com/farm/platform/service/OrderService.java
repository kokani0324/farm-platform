package com.farm.platform.service;

import com.farm.platform.dto.CheckoutRequest;
import com.farm.platform.dto.OrderResponse;
import com.farm.platform.dto.PageResponse;
import com.farm.platform.entity.*;
import com.farm.platform.repository.OrderRepository;
import com.farm.platform.repository.ProductRepository;
import com.farm.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /* ============ 結帳 ============ */

    @Transactional
    public List<OrderResponse> checkout(String email, CheckoutRequest req) {
        User consumer = getUser(email);

        Map<Long, Integer> raw = cartService.getRawCart(consumer.getId());
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("購物車是空的");
        }

        // 一次撈所有商品
        List<Product> products = productRepository.findAllById(raw.keySet());
        if (products.size() != raw.size()) {
            throw new IllegalArgumentException("購物車中有商品已下架，請重新整理購物車");
        }

        // 驗證可購 + 庫存
        for (Product p : products) {
            int qty = raw.get(p.getId());
            if (p.getStatus() != ProductStatus.ACTIVE) {
                throw new IllegalArgumentException("商品「" + p.getName() + "」已下架");
            }
            if (p.getStock() < qty) {
                throw new IllegalArgumentException("商品「" + p.getName() + "」庫存不足（剩 " + p.getStock() + "）");
            }
        }

        // 依小農分組
        Map<User, List<Product>> byFarmer = new LinkedHashMap<>();
        for (Product p : products) {
            byFarmer.computeIfAbsent(p.getFarmer(), k -> new ArrayList<>()).add(p);
        }

        // 為每個小農建立一張訂單
        List<Order> created = new ArrayList<>();
        for (Map.Entry<User, List<Product>> entry : byFarmer.entrySet()) {
            User farmer = entry.getKey();
            List<Product> myProducts = entry.getValue();

            Order order = Order.builder()
                    .orderNo(generateOrderNo())
                    .consumer(consumer)
                    .farmer(farmer)
                    .status(OrderStatus.PENDING_PAYMENT)
                    .paymentMethod(req.getPaymentMethod())
                    .recipientName(req.getRecipientName())
                    .recipientPhone(req.getRecipientPhone())
                    .shippingAddress(req.getShippingAddress())
                    .note(req.getNote())
                    .totalAmount(BigDecimal.ZERO)
                    .build();

            BigDecimal sum = BigDecimal.ZERO;
            for (Product p : myProducts) {
                int qty = raw.get(p.getId());
                BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(qty));

                OrderItem item = OrderItem.builder()
                        .product(p)
                        .productName(p.getName())
                        .unit(p.getUnit())
                        .imageUrl(p.getImageUrl())
                        .unitPrice(p.getPrice())
                        .quantity(qty)
                        .subtotal(subtotal)
                        .build();
                order.addItem(item);

                // 扣庫存
                p.setStock(p.getStock() - qty);
                if (p.getStock() == 0) {
                    p.setStatus(ProductStatus.SOLD_OUT);
                }

                sum = sum.add(subtotal);
            }
            order.setTotalAmount(sum);
            created.add(orderRepository.save(order));
        }

        // 清空購物車
        cartService.clearByUserId(consumer.getId());

        return created.stream().map(OrderResponse::from).toList();
    }

    /* ============ 查詢 ============ */

    public PageResponse<OrderResponse> myOrders(String email, Pageable pageable) {
        User me = getUser(email);
        Page<Order> page = orderRepository.findByConsumerOrderByCreatedAtDesc(me, pageable);
        return PageResponse.of(page, OrderResponse::from);
    }

    public PageResponse<OrderResponse> farmerOrders(String email, Pageable pageable) {
        User me = getUser(email);
        if (!me.hasRole(Role.FARMER)) {
            throw new AccessDeniedException("非小農");
        }
        Page<Order> page = orderRepository.findByFarmerOrderByCreatedAtDesc(me, pageable);
        return PageResponse.of(page, OrderResponse::from);
    }

    public OrderResponse getDetail(String email, Long orderId) {
        Order o = orderRepository.findFullById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));
        if (!o.getConsumer().getEmail().equals(email)
                && !o.getFarmer().getEmail().equals(email)) {
            throw new AccessDeniedException("無權查看此訂單");
        }
        return OrderResponse.from(o);
    }

    /* ============ 付款 / 取消 ============ */

    @Transactional
    public OrderResponse pay(String email, Long orderId) {
        Order o = orderRepository.findFullById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));
        if (!o.getConsumer().getEmail().equals(email)) {
            throw new AccessDeniedException("無權付款此訂單");
        }
        if (o.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("訂單狀態不允許付款（目前：" + o.getStatus() + "）");
        }
        o.setStatus(OrderStatus.PAID);
        o.setPaidAt(LocalDateTime.now());
        return OrderResponse.from(o);
    }

    @Transactional
    public OrderResponse cancel(String email, Long orderId) {
        Order o = orderRepository.findFullById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));
        if (!o.getConsumer().getEmail().equals(email)) {
            throw new AccessDeniedException("無權取消此訂單");
        }
        if (o.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("已付款訂單不可取消，請聯絡小農");
        }

        // 還原庫存
        for (OrderItem item : o.getItems()) {
            Product p = item.getProduct();
            if (p == null) continue;
            p.setStock(p.getStock() + item.getQuantity());
            if (p.getStatus() == ProductStatus.SOLD_OUT && p.getStock() > 0) {
                p.setStatus(ProductStatus.ACTIVE);
            }
        }
        o.setStatus(OrderStatus.CANCELLED);
        return OrderResponse.from(o);
    }

    /* ============ 小農出貨 ============ */

    @Transactional
    public OrderResponse markShipped(String email, Long orderId) {
        Order o = orderRepository.findFullById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));
        if (!o.getFarmer().getEmail().equals(email)) {
            throw new AccessDeniedException("非該訂單的小農");
        }
        if (o.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("只有已付款的訂單可以出貨");
        }
        o.setStatus(OrderStatus.SHIPPED);
        return OrderResponse.from(o);
    }

    /* ============ 消費者確認收貨 ============ */

    @Transactional
    public OrderResponse confirmReceipt(String email, Long orderId) {
        Order o = orderRepository.findFullById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));
        if (!o.getConsumer().getEmail().equals(email)) {
            throw new AccessDeniedException("無權確認此訂單");
        }
        if (o.getStatus() != OrderStatus.SHIPPED) {
            throw new IllegalStateException("只有已出貨的訂單可以確認收貨");
        }
        o.setStatus(OrderStatus.COMPLETED);
        return OrderResponse.from(o);
    }

    /* ============ helpers ============ */

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("使用者不存在"));
    }

    /** NONG-yyyyMMdd-XXXX，重複則重試（理論上極少發生） */
    private String generateOrderNo() {
        String date = LocalDate.now().format(DATE_FMT);
        for (int i = 0; i < 5; i++) {
            String no = "NONG-" + date + "-" + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            if (!orderRepository.existsByOrderNo(no)) {
                return no;
            }
        }
        // 撞五次 → 改用毫秒尾碼
        return "NONG-" + date + "-" + System.currentTimeMillis() % 100000;
    }
}
