package com.farm.platform.controller;

import com.farm.platform.dto.AuthResponse;
import com.farm.platform.dto.SwitchRoleRequest;
import com.farm.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/** 已登入會員的帳號相關操作(在 /api/account/** 下,預設要驗證) */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AuthService authService;

    /** 切換 active role(需此使用者實際擁有該角色),回傳新的 JWT */
    @PostMapping("/switch-role")
    public AuthResponse switchRole(@AuthenticationPrincipal UserDetails me,
                                   @Valid @RequestBody SwitchRoleRequest req) {
        return authService.switchRole(me.getUsername(), req.getRole());
    }
}
