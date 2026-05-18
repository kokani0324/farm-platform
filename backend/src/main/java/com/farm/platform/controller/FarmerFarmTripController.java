package com.farm.platform.controller;

import com.farm.platform.dto.*;
import com.farm.platform.entity.FarmTripOrderStatus;
import com.farm.platform.service.FarmTripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farmer")
@RequiredArgsConstructor
public class FarmerFarmTripController {

    private final FarmTripService service;

    /* ===== 活動本體 CRUD ===== */

    @GetMapping("/farm-trips")
    public PageResponse<FarmTripResponse> myTrips(@AuthenticationPrincipal UserDetails me,
                                                  @PageableDefault(size = 10) Pageable pageable) {
        return service.listFarmerOwn(me.getUsername(), pageable);
    }

    @PostMapping("/farm-trips")
    public FarmTripResponse create(@AuthenticationPrincipal UserDetails me,
                                   @Valid @RequestBody FarmTripRequest req) {
        return service.create(me.getUsername(), req);
    }

    @PutMapping("/farm-trips/{id}")
    public FarmTripResponse update(@AuthenticationPrincipal UserDetails me,
                                   @PathVariable Long id,
                                   @Valid @RequestBody FarmTripRequest req) {
        return service.update(me.getUsername(), id, req);
    }

    @PostMapping("/farm-trips/{id}/close")
    public ResponseEntity<Void> close(@AuthenticationPrincipal UserDetails me,
                                      @PathVariable Long id) {
        service.close(me.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    /* ===== 場次 CRUD ===== */

    @PostMapping("/farm-trips/{id}/sessions")
    public FarmTripSessionResponse addSession(@AuthenticationPrincipal UserDetails me,
                                              @PathVariable Long id,
                                              @Valid @RequestBody FarmTripSessionRequest req) {
        return service.addSession(me.getUsername(), id, req);
    }

    @PostMapping("/farm-trips/{id}/sessions/batch")
    public FarmTripSessionBatchResponse batchAddSessions(@AuthenticationPrincipal UserDetails me,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody FarmTripSessionBatchRequest req) {
        return service.batchAddSessions(me.getUsername(), id, req);
    }

    @PutMapping("/farm-trip-sessions/{sessionId}")
    public FarmTripSessionResponse updateSession(@AuthenticationPrincipal UserDetails me,
                                                 @PathVariable Long sessionId,
                                                 @Valid @RequestBody FarmTripSessionRequest req) {
        return service.updateSession(me.getUsername(), sessionId, req);
    }

    @PostMapping("/farm-trip-sessions/{sessionId}/cancel")
    public ResponseEntity<Void> cancelSession(@AuthenticationPrincipal UserDetails me,
                                              @PathVariable Long sessionId) {
        service.cancelSession(me.getUsername(), sessionId);
        return ResponseEntity.noContent().build();
    }

    /* ===== 訂單管理 + 完成（補登重量） ===== */

    @GetMapping("/farm-trip-orders")
    public PageResponse<FarmTripOrderResponse> orders(@AuthenticationPrincipal UserDetails me,
                                                      @RequestParam(required = false) FarmTripOrderStatus status,
                                                      @PageableDefault(size = 10) Pageable pageable) {
        return service.farmerOrders(me.getUsername(), status, pageable);
    }

    @PostMapping("/farm-trip-orders/{orderId}/complete")
    public FarmTripOrderResponse complete(@AuthenticationPrincipal UserDetails me,
                                          @PathVariable Long orderId,
                                          @RequestBody(required = false) FarmTripCompleteRequest req) {
        return service.completeOrder(me.getUsername(), orderId,
                req != null ? req : new FarmTripCompleteRequest());
    }
}
