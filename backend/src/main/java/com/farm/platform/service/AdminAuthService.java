package com.farm.platform.service;

import com.farm.platform.dto.AuthResponse;
import com.farm.platform.entity.AccountStatus;
import com.farm.platform.entity.AccountType;
import com.farm.platform.entity.Admin;
import com.farm.platform.repository.AdminRepository;
import com.farm.platform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 管理員只能登入,不提供註冊 API(由 DataSeeder 或既有 admin 後台建立) */
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String rawPassword) {
        Admin a = adminRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("帳號或密碼錯誤"));
        if (!passwordEncoder.matches(rawPassword, a.getPassword())) {
            throw new IllegalArgumentException("帳號或密碼錯誤");
        }
        if (a.getStatus() != AccountStatus.NORMAL) {
            throw new IllegalStateException("此管理員帳號目前狀態:" + a.getStatus());
        }
        String token = jwtUtil.generateToken(a.getEmail(), AccountType.ADMIN, a.getId());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .accountId(a.getId())
                .type(AccountType.ADMIN)
                .email(a.getEmail())
                .name(a.getName())
                .build();
    }
}
