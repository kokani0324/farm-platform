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

    @GetMapping("/categories")
    public List<FarmTripCategoryResponse> categories() {
        return service.listCategories();
    }

    @GetMapping
    public PageResponse<FarmTripResponse> list(@RequestParam(required = false) Long categoryId,
                                               @RequestParam(required = false) TripType tripType,
                                               @PageableDefault(size = 12) Pageable pageable) {
        return service.listPublic(categoryId, tripType, pageable);
    }

    @GetMapping("/{id}")
    public FarmTripResponse detail(@PathVariable Long id) {
        return service.getDetail(id);
    }

    /* ===== 消費者:預約 ===== */

    @PostMapping("/{id}/bookings")
    public FarmTripBookingResponse book(@AuthenticationPrincipal UserDetails me,
                                        @PathVariable Long id,
                                        @Valid @RequestBody CreateFarmTripBookingRequest req) {
        return service.book(me.getUsername(), id, req);
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public FarmTripBookingResponse cancel(@AuthenticationPrincipal UserDetails me,
                                          @PathVariable Long bookingId) {
        return service.cancelBooking(me.getUsername(), bookingId);
    }

    @GetMapping("/bookings/mine")
    public PageResponse<FarmTripBookingResponse> mine(@AuthenticationPrincipal UserDetails me,
                                                      @PageableDefault(size = 10) Pageable pageable) {
        return service.myBookings(me.getUsername(), pageable);
    }
}
