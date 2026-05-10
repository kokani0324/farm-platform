package com.farm.platform.service;

import com.farm.platform.dto.AdminStatsResponse;
import com.farm.platform.dto.AdminUserResponse;
import com.farm.platform.dto.PageResponse;
import com.farm.platform.dto.ProductResponse;
import com.farm.platform.entity.*;
import com.farm.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminService {

    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final GroupBuyRepository groupBuyRepo;
    private final FarmTripRepository farmTripRepo;
    private final BlogService blogService;

    /* ============================ Stats ============================ */

    public AdminStatsResponse stats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepo.count())
                .totalConsumers(userRepo.countByRole(Role.CONSUMER))
                .totalFarmers(userRepo.countByRole(Role.FARMER))
                .totalAdmins(userRepo.countByRole(Role.ADMIN))
                .disabledUsers(userRepo.countByEnabledFalse())
                .totalProducts(productRepo.count())
                .activeProducts(productRepo.countByStatus(ProductStatus.ACTIVE))
                .totalOrders(orderRepo.count())
                .totalGroupBuys(groupBuyRepo.count())
                .openGroupBuys(groupBuyRepo.countByStatus(GroupBuyStatus.OPEN))
                .totalFarmTrips(farmTripRepo.count())
                .totalBlogs(blogService.countBlogs())
                .pendingBlogReports(blogService.countPendingReports())
                .build();
    }

    /* ============================ Users ============================ */

    public PageResponse<AdminUserResponse> listUsers(String keyword, Pageable pageable) {
        Page<User> page = userRepo.searchAdminList(keyword, pageable);
        return PageResponse.of(page, AdminUserResponse::from);
    }

    @Transactional
    public AdminUserResponse setEnabled(Long id, boolean enabled) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));
        if (u.getRole() == Role.ADMIN) {
            throw new IllegalStateException("無法停用管理員帳號");
        }
        u.setEnabled(enabled);
        return AdminUserResponse.from(u);
    }

    /* ============================ Products ============================ */

    public PageResponse<ProductResponse> listProducts(String keyword, Pageable pageable) {
        Page<Product> page = productRepo.adminSearch(keyword, pageable);
        return PageResponse.of(page, ProductResponse::from);
    }

    @Transactional
    public ProductResponse setProductStatus(Long id, ProductStatus status) {
        Product p = productRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        p.setStatus(status);
        return ProductResponse.from(p);
    }
}
