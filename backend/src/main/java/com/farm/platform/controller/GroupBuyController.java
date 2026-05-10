package com.farm.platform.controller;

import com.farm.platform.dto.*;
import com.farm.platform.service.GroupBuyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/group-buys")
@RequiredArgsConstructor
public class GroupBuyController {

    private final GroupBuyService service;

    /* ===== 公開瀏覽 ===== */

    @GetMapping
    public PageResponse<GroupBuyResponse> listOpen(@AuthenticationPrincipal UserDetails me,
                                                   @PageableDefault(size = 12) Pageable pageable) {
        return service.listOpen(pageable, me != null ? me.getUsername() : null);
    }

    @GetMapping("/{id}")
    public GroupBuyResponse detail(@AuthenticationPrincipal UserDetails me,
                                   @PathVariable Long id) {
        return service.getDetail(id, me != null ? me.getUsername() : null);
    }

    /* ===== 消費者:發起請求 ===== */

    @PostMapping("/requests")
    public GroupBuyRequestResponse createRequest(@AuthenticationPrincipal UserDetails me,
                                                 @Valid @RequestBody CreateGroupBuyRequest req) {
        return service.createRequest(me.getUsername(), req);
    }

    @GetMapping("/requests/mine")
    public PageResponse<GroupBuyRequestResponse> myRequests(@AuthenticationPrincipal UserDetails me,
                                                            @PageableDefault(size = 10) Pageable pageable) {
        return service.myRequests(me.getUsername(), pageable);
    }

    @PostMapping("/requests/{id}/withdraw")
    public GroupBuyRequestResponse withdrawRequest(@AuthenticationPrincipal UserDetails me,
                                                   @PathVariable Long id) {
        return service.withdrawRequest(me.getUsername(), id);
    }

    /* ===== 消費者:加入 / 退出 / 我參加的 ===== */

    @PostMapping("/{id}/join")
    public ParticipationResponse join(@AuthenticationPrincipal UserDetails me,
                                      @PathVariable Long id,
                                      @Valid @RequestBody JoinGroupBuyRequest req) {
        return service.join(me.getUsername(), id, req);
    }

    @PostMapping("/{id}/withdraw")
    public ParticipationResponse withdraw(@AuthenticationPrincipal UserDetails me,
                                          @PathVariable Long id) {
        return service.withdraw(me.getUsername(), id);
    }

    @GetMapping("/participations/mine")
    public PageResponse<ParticipationResponse> myParticipations(@AuthenticationPrincipal UserDetails me,
                                                                @PageableDefault(size = 10) Pageable pageable) {
        return service.myParticipations(me.getUsername(), pageable);
    }
}
