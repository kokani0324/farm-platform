package com.farm.platform.controller;

import com.farm.platform.dto.*;
import com.farm.platform.service.FarmTripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/farm-trips")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminFarmTripController {

    private final FarmTripService service;

    @GetMapping("/pending")
    public PageResponse<FarmTripResponse> pending(@PageableDefault(size = 10) Pageable pageable) {
        return service.adminListPending(pageable);
    }

    @PostMapping("/{id}/audit")
    public FarmTripAuditResponse audit(@AuthenticationPrincipal UserDetails me,
                                       @PathVariable Long id,
                                       @Valid @RequestBody FarmTripAuditRequest req) {
        return service.adminAudit(me.getUsername(), id, req);
    }

    @GetMapping("/{id}/audits")
    public List<FarmTripAuditResponse> history(@PathVariable Long id) {
        return service.auditHistory(id);
    }
}
