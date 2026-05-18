package com.farm.platform.controller;

import com.farm.platform.dto.*;
import com.farm.platform.entity.TripType;
import com.farm.platform.service.FarmTripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farm-trips")
@RequiredArgsConstructor
public class FarmTripController {

    private final FarmTripService service;

    /* ===== 公開瀏覽 ===== */

    @GetMapping
    public PageResponse<FarmTripResponse> list(@RequestParam(required = false) TripType tripType,
                                               @PageableDefault(size = 12) Pageable pageable) {
        return service.listPublic(tripType, pageable);
    }

    @GetMapping("/{id}")
    public FarmTripResponse detail(@PathVariable Long id) {
        return service.getDetail(id);
    }

    @GetMapping("/{id}/comments")
    public List<FarmTripCommentResponse> comments(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "20") int size) {
        return service.listComments(id, Math.min(size, 100));
    }

    /* ===== 消費者：預約 / 評論 ===== */

    @PostMapping("/sessions/{sessionId}/orders")
    public FarmTripOrderResponse book(@AuthenticationPrincipal UserDetails me,
                                      @PathVariable Long sessionId,
                                      @Valid @RequestBody CreateFarmTripOrderRequest req) {
        return service.bookSession(me.getUsername(), sessionId, req);
    }

    @PostMapping("/orders/{orderId}/cancel")
    public FarmTripOrderResponse cancel(@AuthenticationPrincipal UserDetails me,
                                        @PathVariable Long orderId) {
        return service.cancelOrder(me.getUsername(), orderId);
    }

    @GetMapping("/orders/mine")
    public PageResponse<FarmTripOrderResponse> mine(@AuthenticationPrincipal UserDetails me,
                                                    @PageableDefault(size = 10) Pageable pageable) {
        return service.myOrders(me.getUsername(), pageable);
    }

    @PostMapping("/{id}/comments")
    public FarmTripCommentResponse postComment(@AuthenticationPrincipal UserDetails me,
                                               @PathVariable Long id,
                                               @Valid @RequestBody FarmTripCommentRequest req) {
        return service.comment(me.getUsername(), id, req);
    }
}
