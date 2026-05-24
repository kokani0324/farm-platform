package com.farm.platform.account.service;

import com.farm.platform.account.dto.AuthResponse;
import com.farm.platform.account.dto.FarmerRegisterRequest;
import com.farm.platform.account.entity.AccountStatus;
import com.farm.platform.account.entity.AccountType;
import com.farm.platform.account.entity.Farmer;
import com.farm.platform.account.repository.FarmerRepository;
import com.farm.platform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class FarmerAuthService {

    private final FarmerRepository farmerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void register(FarmerRegisterRequest req) {
        if (farmerRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("此 Email 已被註冊為小農");
        }
        byte[] certBytes = null;
        if (req.getCertFileBase64() != null && !req.getCertFileBase64().isBlank()) {
            try {
                String b64 = req.getCertFileBase64();
                int comma = b64.indexOf(',');
                if (b64.startsWith("data:") && comma > 0) b64 = b64.substring(comma + 1); // 去掉 data URL 前綴
                certBytes = Base64.getDecoder().decode(b64);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("證明文件編碼錯誤");
            }
        }
        Farmer f = Farmer.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .farmName(req.getFarmName())
                .farmAddress(req.getFarmAddress())
                .phone(req.getPhone())
                .farmDesc(req.getFarmDesc())
                .locLat(req.getLocLat())
                .locLong(req.getLocLong())
                .certType(req.getCertType())
                .certFile(certBytes)
                .uploadedAt(certBytes == null ? null : LocalDateTime.now())
                .certPassed(false) // 預設待審
                .build();
        farmerRepository.save(f);
        // 註冊不發 token,需通過審核後才能登入
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String rawPassword) {
        Farmer f = farmerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("帳號或密碼錯誤"));
        if (!passwordEncoder.matches(rawPassword, f.getPassword())) {
            throw new IllegalArgumentException("帳號或密碼錯誤");
        }
        if (!Boolean.TRUE.equals(f.getCertPassed())) {
            throw new IllegalStateException("小農帳號尚待管理員審核");
        }
        if (f.getStatus() != AccountStatus.NORMAL) {
            throw new IllegalStateException("此帳號目前狀態:" + f.getStatus());
        }
        String token = jwtUtil.generateToken(f.getEmail(), AccountType.FARMER, f.getId());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .accountId(f.getId())
                .type(AccountType.FARMER)
                .email(f.getEmail())
                .name(f.getFarmName())
                .build();
    }
}
