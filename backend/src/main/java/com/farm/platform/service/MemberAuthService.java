package com.farm.platform.service;

import com.farm.platform.dto.AuthResponse;
import com.farm.platform.dto.MemberRegisterRequest;
import com.farm.platform.entity.AccountStatus;
import com.farm.platform.entity.AccountType;
import com.farm.platform.entity.Member;
import com.farm.platform.repository.MemberRepository;
import com.farm.platform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberAuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(MemberRegisterRequest req) {
        if (memberRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("此 Email 已被註冊為會員");
        }
        Member m = Member.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .nickname(req.getNickname())
                .phone(req.getPhone())
                .address(req.getAddress())
                .build();
        Member saved = memberRepository.save(m);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String rawPassword) {
        Member m = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("帳號或密碼錯誤"));
        if (!passwordEncoder.matches(rawPassword, m.getPassword())) {
            throw new IllegalArgumentException("帳號或密碼錯誤");
        }
        if (m.getStatus() != AccountStatus.NORMAL) {
            throw new IllegalStateException("此帳號目前狀態:" + m.getStatus());
        }
        return toResponse(m);
    }

    private AuthResponse toResponse(Member m) {
        String token = jwtUtil.generateToken(m.getEmail(), AccountType.MEMBER, m.getId());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .accountId(m.getId())
                .type(AccountType.MEMBER)
                .email(m.getEmail())
                .name(m.getName())
                .build();
    }
}
