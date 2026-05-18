package com.farm.platform.service;

import com.farm.platform.dto.AdminFarmerReviewResponse;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminService {

    private final MemberRepository memberRepo;
    private final FarmerRepository farmerRepo;
    private final AdminRepository adminRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final GroupBuyRepository groupBuyRepo;
    private final FarmTripRepository farmTripRepo;
    private final BlogService blogService;

    /* ============================ Stats ============================ */

    public AdminStatsResponse stats() {
        long members = memberRepo.count();
        long farmers = farmerRepo.count();
        long admins = adminRepo.count();
        long disabled = memberRepo.countByStatus(AccountStatus.SUSPENDED)
                + farmerRepo.countByStatus(AccountStatus.SUSPENDED);
        return AdminStatsResponse.builder()
                .totalUsers(members + farmers + admins)
                .totalConsumers(members)
                .totalFarmers(farmers)
                .totalAdmins(admins)
                .disabledUsers(disabled)
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

    /* ============================ Members ============================ */

    public PageResponse<AdminUserResponse> listMembers(String keyword, Pageable pageable) {
        Page<Member> page = memberRepo.searchAdminList(keyword, pageable);
        return PageResponse.of(page, AdminUserResponse::fromMember);
    }

    @Transactional
    public AdminUserResponse setMemberEnabled(Long id, boolean enabled) {
        Member m = memberRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("會員不存在"));
        m.setStatus(enabled ? AccountStatus.NORMAL : AccountStatus.SUSPENDED);
        return AdminUserResponse.fromMember(m);
    }

    /* ============================ Farmers ============================ */

    public PageResponse<AdminUserResponse> listFarmers(String keyword, Pageable pageable) {
        Page<Farmer> page = farmerRepo.searchAdminList(keyword, pageable);
        return PageResponse.of(page, AdminUserResponse::fromFarmer);
    }

    @Transactional
    public AdminUserResponse setFarmerEnabled(Long id, boolean enabled) {
        Farmer f = farmerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("小農不存在"));
        f.setStatus(enabled ? AccountStatus.NORMAL : AccountStatus.SUSPENDED);
        return AdminUserResponse.fromFarmer(f);
    }

    /** 待審小農列表（cert_passed=false） */
    public List<AdminFarmerReviewResponse> listPendingFarmers() {
        return farmerRepo.findByCertPassedFalseOrderByCreatedAtDesc().stream()
                .map(AdminFarmerReviewResponse::from)
                .toList();
    }

    public AdminFarmerReviewResponse getFarmerDetail(Long id) {
        Farmer f = farmerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("小農不存在"));
        return AdminFarmerReviewResponse.from(f);
    }

    /** 通過/駁回小農送審 */
    @Transactional
    public AdminFarmerReviewResponse setFarmerCertPassed(Long id, boolean passed) {
        Farmer f = farmerRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("小農不存在"));
        f.setCertPassed(passed);
        return AdminFarmerReviewResponse.from(f);
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
