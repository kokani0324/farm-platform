package com.farm.platform.controller;

import com.farm.platform.dto.*;
import com.farm.platform.entity.ProductStatus;
import com.farm.platform.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return service.stats();
    }

    /* ===== Users ===== */

    @GetMapping("/users")
    public PageResponse<AdminUserResponse> listUsers(@RequestParam(required = false) String keyword,
                                                     @PageableDefault(size = 15) Pageable pageable) {
        return service.listUsers(keyword, pageable);
    }

    @PostMapping("/users/{id}/enable")
    public AdminUserResponse enableUser(@PathVariable Long id) {
        return service.setEnabled(id, true);
    }

    @PostMapping("/users/{id}/disable")
    public AdminUserResponse disableUser(@PathVariable Long id) {
        return service.setEnabled(id, false);
    }

    /* ===== Products ===== */

    @GetMapping("/products")
    public PageResponse<ProductResponse> listProducts(@RequestParam(required = false) String keyword,
                                                      @PageableDefault(size = 15) Pageable pageable) {
        return service.listProducts(keyword, pageable);
    }

    @PostMapping("/products/{id}/take-down")
    public ProductResponse takeDown(@PathVariable Long id) {
        return service.setProductStatus(id, ProductStatus.INACTIVE);
    }

    @PostMapping("/products/{id}/restore")
    public ProductResponse restore(@PathVariable Long id) {
        return service.setProductStatus(id, ProductStatus.ACTIVE);
    }
}
