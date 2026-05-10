package com.farm.platform.controller;

import com.farm.platform.dto.ProductRequest;
import com.farm.platform.dto.ProductResponse;
import com.farm.platform.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小農自己的商品管理（需 ROLE_FARMER）
 */
@RestController
@RequestMapping("/api/farmer/products")
@RequiredArgsConstructor
public class FarmerProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductResponse> myProducts(@AuthenticationPrincipal UserDetails me) {
        return productService.listMine(me.getUsername());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@AuthenticationPrincipal UserDetails me,
                                                  @Valid @RequestBody ProductRequest req) {
        return ResponseEntity.ok(productService.create(me.getUsername(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@AuthenticationPrincipal UserDetails me,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody ProductRequest req) {
        return ResponseEntity.ok(productService.update(me.getUsername(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails me,
                                       @PathVariable Long id) {
        productService.delete(me.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    /** 切換上下架（ACTIVE ↔ INACTIVE） */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ProductResponse> toggle(@AuthenticationPrincipal UserDetails me,
                                                  @PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleStatus(me.getUsername(), id));
    }
}
