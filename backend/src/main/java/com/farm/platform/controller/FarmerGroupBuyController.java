package com.farm.platform.controller;

import com.farm.platform.dto.GroupBuyOrderResponse;
import com.farm.platform.dto.GroupBuyRequestResponse;
import com.farm.platform.dto.GroupBuyResponse;
import com.farm.platform.dto.PageResponse;
import com.farm.platform.dto.ParticipationResponse;
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

    /** 我名下的團購整單列表 */
    @GetMapping("/group-buy-orders")
    public PageResponse<GroupBuyOrderResponse> myGroupBuyOrders(@AuthenticationPrincipal UserDetails me,
                                                                @PageableDefault(size = 10) Pageable pageable) {
        return service.farmerGroupBuyOrders(me.getUsername(), pageable);
    }

    /** 取得某團購的整單明細（含參與者清單） */
    @GetMapping("/group-buys/{id}/order")
    public GroupBuyOrderResponse getGroupBuyOrder(@AuthenticationPrincipal UserDetails me,
                                                  @PathVariable Long id) {
        return service.getGroupBuyOrder(me.getUsername(), id);
    }

    /** 標記某團員 participation 已出貨 */
    @PostMapping("/group-buys/{id}/participations/{pid}/ship")
    public ParticipationResponse markShipped(@AuthenticationPrincipal UserDetails me,
                                             @PathVariable Long id,
                                             @PathVariable Long pid) {
        return service.markParticipationShipped(me.getUsername(), id, pid);
    }
}
