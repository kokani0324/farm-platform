package com.farm.platform.controller;

import com.farm.platform.dto.*;
import com.farm.platform.entity.FarmTripBookingStatus;
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

    @PostMapping("/farm-trips/{id}/cancel")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserDetails me,
                                       @PathVariable Long id) {
        service.cancel(me.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/farm-trip-bookings")
    public PageResponse<FarmTripBookingResponse> bookings(@AuthenticationPrincipal UserDetails me,
                                                          @RequestParam(required = false) FarmTripBookingStatus status,
                                                          @PageableDefault(size = 10) Pageable pageable) {
        return service.farmerBookings(me.getUsername(), status, pageable);
    }
}
