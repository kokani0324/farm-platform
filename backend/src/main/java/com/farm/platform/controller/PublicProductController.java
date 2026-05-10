package com.farm.platform.controller;

import com.farm.platform.dto.CategoryResponse;
import com.farm.platform.dto.PageResponse;
import com.farm.platform.dto.ProductResponse;
import com.farm.platform.service.CategoryService;
import com.farm.platform.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公開瀏覽用 API（無需登入）
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return categoryService.listAll();
    }

    /**
     * 商品列表
     * 範例：/api/public/products?page=0&size=12&sort=createdAt,desc&categoryId=1&keyword=蘋果
     */
    @GetMapping("/products")
    public PageResponse<ProductResponse> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 12) Pageable pageable
    ) {
        return productService.searchActive(categoryId, keyword, pageable);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getDetail(id));
    }
}
