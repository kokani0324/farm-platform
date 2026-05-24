package com.farm.platform.account.controller;

import com.farm.platform.account.dto.AuthResponse;
import com.farm.platform.account.dto.ChangePasswordRequest;
import com.farm.platform.account.dto.MemberMeResponse;
import com.farm.platform.account.dto.UpdateMemberProfileRequest;
import com.farm.platform.account.entity.AccountType;
import com.farm.platform.security.AccountPrincipal;
import com.farm.platform.account.service.MemberProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** 已登入會員的帳號操作。 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final MemberProfileService memberProfileService;

    @GetMapping("/me")
    public MemberMeResponse me(@AuthenticationPrincipal AccountPrincipal me) {
        ensureMember(me);
        return memberProfileService.getMe(me.getId());
    }

    @PutMapping("/profile")
    public AuthResponse updateProfile(@AuthenticationPrincipal AccountPrincipal me,
                                      @Valid @RequestBody UpdateMemberProfileRequest req) {
        ensureMember(me);
        return memberProfileService.updateProfile(me.getId(), req);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AccountPrincipal me,
                                               @Valid @RequestBody ChangePasswordRequest req) {
        ensureMember(me);
        memberProfileService.changePassword(me.getId(), req.getOldPassword(), req.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    private void ensureMember(AccountPrincipal me) {
        if (me == null || me.getType() != AccountType.MEMBER) {
            throw new AccessDeniedException("此功能僅限會員使用");
        }
    }
}
