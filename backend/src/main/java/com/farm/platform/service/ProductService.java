package com.farm.platform.service;

import com.farm.platform.dto.PageResponse;
import com.farm.platform.dto.ProductRequest;
import com.farm.platform.dto.ProductResponse;
import com.farm.platform.entity.*;
import com.farm.platform.repository.ProductRepository;
import com.farm.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;

    /* ========== 公開：瀏覽 ========== */

    public PageResponse<ProductResponse> searchActive(Long categoryId, String keyword, Pageable pageable) {
        Page<Product> page = productRepository.search(ProductStatus.ACTIVE, categoryId, keyword, pageable);
        return PageResponse.of(page, ProductResponse::from);
    }

    public ProductResponse getDetail(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在 id=" + id));
        return ProductResponse.from(p);
    }

    /* ========== 小農：管理 ========== */

    public List<ProductResponse> listMine(String farmerEmail) {
        User farmer = getFarmer(farmerEmail);
        return productRepository.findByFarmerOrderByCreatedAtDesc(farmer)
                .stream().map(ProductResponse::from).toList();
    }

    @Transactional
    public ProductResponse create(String farmerEmail, ProductRequest req) {
        User farmer = getFarmer(farmerEmail);
        Category category = categoryService.getById(req.getCategoryId());

        Product p = Product.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .unit(req.getUnit())
                .stock(req.getStock())
                .imageUrl(req.getImageUrl())
                .origin(req.getOrigin())
                .shippingMethod(req.getShippingMethod())
                .groupBuyEnabled(Boolean.TRUE.equals(req.getGroupBuyEnabled()))
                .status(req.getStock() == 0 ? ProductStatus.SOLD_OUT : ProductStatus.ACTIVE)
                .farmer(farmer)
                .category(category)
                .build();

        return ProductResponse.from(productRepository.save(p));
    }

    @Transactional
    public ProductResponse update(String farmerEmail, Long productId, ProductRequest req) {
        Product p = loadAndCheckOwnership(farmerEmail, productId);
        Category category = categoryService.getById(req.getCategoryId());

        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setUnit(req.getUnit());
        p.setStock(req.getStock());
        p.setImageUrl(req.getImageUrl());
        p.setOrigin(req.getOrigin());
        p.setShippingMethod(req.getShippingMethod());
        p.setGroupBuyEnabled(Boolean.TRUE.equals(req.getGroupBuyEnabled()));
        p.setCategory(category);

        // 庫存=0 自動售完；售完狀態若庫存恢復則 ACTIVE
        if (p.getStock() == 0) {
            p.setStatus(ProductStatus.SOLD_OUT);
        } else if (p.getStatus() == ProductStatus.SOLD_OUT) {
            p.setStatus(ProductStatus.ACTIVE);
        }

        return ProductResponse.from(p);
    }

    @Transactional
    public void delete(String farmerEmail, Long productId) {
        Product p = loadAndCheckOwnership(farmerEmail, productId);
        productRepository.delete(p);
    }

    @Transactional
    public ProductResponse toggleStatus(String farmerEmail, Long productId) {
        Product p = loadAndCheckOwnership(farmerEmail, productId);
        if (p.getStatus() == ProductStatus.ACTIVE) {
            p.setStatus(ProductStatus.INACTIVE);
        } else if (p.getStatus() == ProductStatus.INACTIVE) {
            p.setStatus(p.getStock() == 0 ? ProductStatus.SOLD_OUT : ProductStatus.ACTIVE);
        }
        return ProductResponse.from(p);
    }

    /* ========== helpers ========== */

    private User getFarmer(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("使用者不存在"));
        if (!u.hasRole(Role.FARMER)) {
            throw new AccessDeniedException("只有小農可以管理商品");
        }
        return u;
    }

    private Product loadAndCheckOwnership(String farmerEmail, Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在 id=" + productId));
        if (!p.getFarmer().getEmail().equals(farmerEmail)) {
            throw new AccessDeniedException("無權操作他人商品");
        }
        return p;
    }
}
