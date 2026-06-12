package com.farm.platform.account.controller;

import com.farm.platform.account.dto.PublicFarmerResponse;
import com.farm.platform.account.service.PublicFarmerService;
import com.farm.platform.blog.dto.BlogResponse;
import com.farm.platform.blog.service.BlogService;
import com.farm.platform.common.dto.PageResponse;
import com.farm.platform.farmtrip.dto.FarmTripResponse;
import com.farm.platform.farmtrip.service.FarmTripService;
import com.farm.platform.groupbuy.dto.GroupBuyResponse;
import com.farm.platform.groupbuy.service.GroupBuyService;
import com.farm.platform.shop.dto.ProductResponse;
import com.farm.platform.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/farmers")
@RequiredArgsConstructor
public class PublicFarmerController {

    private final PublicFarmerService publicFarmerService;
    private final ProductService productService;
    private final GroupBuyService groupBuyService;
    private final FarmTripService farmTripService;
    private final BlogService blogService;

    @GetMapping
    public PageResponse<PublicFarmerResponse> list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 12) Pageable pageable
    ) {
        return publicFarmerService.listPublic(keyword, pageable);
    }

    @GetMapping("/{id}")
    public PublicFarmerResponse detail(@PathVariable Long id) {
        return publicFarmerService.getPublicDetail(id);
    }

    @GetMapping("/{id}/products")
    public PageResponse<ProductResponse> products(@PathVariable Long id,
                                                  @PageableDefault(size = 8) Pageable pageable) {
        return productService.searchActiveByFarmer(id, pageable);
    }

    @GetMapping("/{id}/group-buys")
    public PageResponse<GroupBuyResponse> groupBuys(@AuthenticationPrincipal UserDetails me,
                                                    @PathVariable Long id,
                                                    @PageableDefault(size = 6) Pageable pageable) {
        return groupBuyService.listOpenByFarmer(id, pageable, me != null ? me.getUsername() : null);
    }

    @GetMapping("/{id}/farm-trips")
    public PageResponse<FarmTripResponse> farmTrips(@PathVariable Long id,
                                                    @PageableDefault(size = 6) Pageable pageable) {
        return farmTripService.listPublicByFarmer(id, pageable);
    }

    @GetMapping("/{id}/blogs")
    public PageResponse<BlogResponse> blogs(@PathVariable Long id,
                                            @PageableDefault(size = 8) Pageable pageable) {
        return blogService.listPublicByFarmer(id, pageable);
    }
}
