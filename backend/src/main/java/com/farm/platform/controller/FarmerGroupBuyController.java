package com.farm.platform.controller;

import com.farm.platform.dto.GroupBuyRequestResponse;
import com.farm.platform.dto.GroupBuyResponse;
import com.farm.platform.dto.PageResponse;
import com.farm.platform.dto.ReviewGroupBuyRequest;
import com.farm.platform.entity.GroupBuyRequestStatus;
import com.farm.platform.service.GroupBuyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farmer")
@RequiredArgsConstructor
public class FarmerGroupBuyController {

    private final GroupBuyService service;

    /** 收到的團購請求列表(可選 status 過濾) */
    @GetMapping("/group-buy-requests")
    public PageResponse<GroupBuyRequestResponse> myRequests(@AuthenticationPrincipal UserDetails me,
                                                            @RequestParam(required = false) GroupBuyRequestStatus status,
                                                            @PageableDefault(size = 10) Pageable pageable) {
        return service.farmerRequests(me.getUsername(), status, pageable);
    }

    /** 審核(通過/拒絕) */
    @PostMapping("/group-buy-requests/{id}/review")
    public GroupBuyRequestResponse review(@AuthenticationPrincipal UserDetails me,
                                          @PathVariable Long id,
                                          @Valid @RequestBody ReviewGroupBuyRequest body) {
        return service.review(me.getUsername(), id, body);
    }

    /** 我的團購活動 */
    @GetMapping("/group-buys")
    public PageResponse<GroupBuyResponse> myGroupBuys(@AuthenticationPrincipal UserDetails me,
                                                      @PageableDefault(size = 10) Pageable pageable) {
        return service.farmerGroupBuys(me.getUsername(), pageable);
    }
}
