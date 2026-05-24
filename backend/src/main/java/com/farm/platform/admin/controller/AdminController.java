package com.farm.platform.admin.controller;

import com.farm.platform.admin.dto.AdminFarmerReviewResponse;
import com.farm.platform.admin.dto.AdminStatsResponse;
import com.farm.platform.admin.dto.AdminUserResponse;
import com.farm.platform.common.dto.PageResponse;
import com.farm.platform.shop.dto.ProductResponse;
import com.farm.platform.shop.entity.ProductStatus;
import com.farm.platform.admin.service.AdminService;
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

    /* ===== Members ===== */

    @GetMapping("/members")
    public PageResponse<AdminUserResponse> listMembers(@RequestParam(required = false) String keyword,
                                                       @PageableDefault(size = 15) Pageable pageable) {
        return service.listMembers(keyword, pageable);
    }

    @PostMapping("/members/{id}/enable")
    public AdminUserResponse enableMember(@PathVariable Long id) {
        return service.setMemberEnabled(id, true);
    }

    @PostMapping("/members/{id}/disable")
    public AdminUserResponse disableMember(@PathVariable Long id) {
        return service.setMemberEnabled(id, false);
    }

    /* ===== Farmers ===== */

    @GetMapping("/farmers")
    public PageResponse<AdminUserResponse> listFarmers(@RequestParam(required = false) String keyword,
                                                       @PageableDefault(size = 15) Pageable pageable) {
        return service.listFarmers(keyword, pageable);
    }

    @PostMapping("/farmers/{id}/enable")
    public AdminUserResponse enableFarmer(@PathVariable Long id) {
        return service.setFarmerEnabled(id, true);
    }

    @PostMapping("/farmers/{id}/disable")
    public AdminUserResponse disableFarmer(@PathVariable Long id) {
        return service.setFarmerEnabled(id, false);
    }

    @GetMapping("/farmers/pending")
    public java.util.List<AdminFarmerReviewResponse> pendingFarmers() {
        return service.listPendingFarmers();
    }

    @GetMapping("/farmers/{id}")
    public AdminFarmerReviewResponse farmerDetail(@PathVariable Long id) {
        return service.getFarmerDetail(id);
    }

    @PostMapping("/farmers/{id}/cert-pass")
    public AdminFarmerReviewResponse passFarmerCert(@PathVariable Long id) {
        return service.setFarmerCertPassed(id, true);
    }

    @PostMapping("/farmers/{id}/cert-reject")
    public AdminFarmerReviewResponse rejectFarmerCert(@PathVariable Long id) {
        return service.setFarmerCertPassed(id, false);
    }

    /* ===== Backwards-compat: /api/admin/users → 預設列出 Members ===== */

    @GetMapping("/users")
    public PageResponse<AdminUserResponse> listUsersLegacy(@RequestParam(required = false) String keyword,
                                                           @PageableDefault(size = 15) Pageable pageable) {
        return service.listMembers(keyword, pageable);
    }

    @PostMapping("/users/{id}/enable")
    public AdminUserResponse enableUserLegacy(@PathVariable Long id) {
        return service.setMemberEnabled(id, true);
    }

    @PostMapping("/users/{id}/disable")
    public AdminUserResponse disableUserLegacy(@PathVariable Long id) {
        return service.setMemberEnabled(id, false);
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
