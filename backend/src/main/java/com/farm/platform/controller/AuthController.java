package com.farm.platform.controller;

import com.farm.platform.dto.AdminLoginRequest;
import com.farm.platform.dto.AuthResponse;
import com.farm.platform.dto.FarmerRegisterRequest;
import com.farm.platform.dto.LoginRequest;
import com.farm.platform.dto.MemberRegisterRequest;
import com.farm.platform.entity.AccountType;
import com.farm.platform.service.AdminAuthService;
import com.farm.platform.service.FarmerAuthService;
import com.farm.platform.service.MemberAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberAuthService memberAuthService;
    private final FarmerAuthService farmerAuthService;
    private final AdminAuthService adminAuthService;

    /** 公開登入入口:會員 / 小農 共用,body.userType 決定要查哪張表 */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        if (req.getUserType() == AccountType.ADMIN) {
            throw new IllegalArgumentException("管理員請使用專屬入口登入");
        }
        return switch (req.getUserType()) {
            case MEMBER -> memberAuthService.login(req.getEmail(), req.getPassword());
            case FARMER -> farmerAuthService.login(req.getEmail(), req.getPassword());
            case ADMIN -> throw new IllegalArgumentException("禁止經此端點以管理員身份登入");
        };
    }

    /** 消費者註冊 */
    @PostMapping("/member/register")
    public AuthResponse memberRegister(@Valid @RequestBody MemberRegisterRequest req) {
        return memberAuthService.register(req);
    }

    /** 小農註冊:不發 token,須待管理員審核(cert_passed=true)後才能登入 */
    @PostMapping("/farmer/register")
    public ResponseEntity<?> farmerRegister(@Valid @RequestBody FarmerRegisterRequest req) {
        farmerAuthService.register(req);
        return ResponseEntity.accepted().body(java.util.Map.of(
                "message", "小農申請已送出,待管理員審核通過後即可登入。"
        ));
    }

    /** 管理員登入(隱藏端點):不在公開導覽出現,前端只有 console-admin-x9k2p.html 會打 */
    @PostMapping("/admin/login")
    public AuthResponse adminLogin(@Valid @RequestBody AdminLoginRequest req) {
        return adminAuthService.login(req.getEmail(), req.getPassword());
    }
}
