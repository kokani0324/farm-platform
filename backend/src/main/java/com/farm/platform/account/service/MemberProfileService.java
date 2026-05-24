package com.farm.platform.account.service;

import com.farm.platform.account.dto.AuthResponse;
import com.farm.platform.account.dto.MemberMeResponse;
import com.farm.platform.account.dto.UpdateMemberProfileRequest;
import com.farm.platform.account.entity.AccountType;
import com.farm.platform.account.entity.Member;
import com.farm.platform.account.repository.MemberRepository;
import com.farm.platform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public MemberMeResponse getMe(Long memberId) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("會員不存在"));
        return MemberMeResponse.from(m);
    }

    @Transactional
    public AuthResponse updateProfile(Long memberId, UpdateMemberProfileRequest req) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("會員不存在"));
        m.setName(req.getName());
        m.setNickname(req.getNickname());
        m.setPhone(req.getPhone());
        m.setAddress(req.getAddress());
        Member saved = memberRepository.save(m);
        // 重簽 token,前端可一次替換 localStorage 內容
        String token = jwtUtil.generateToken(saved.getEmail(), AccountType.MEMBER, saved.getId());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .accountId(saved.getId())
                .type(AccountType.MEMBER)
                .email(saved.getEmail())
                .name(saved.getName())
                .build();
    }

    @Transactional
    public void changePassword(Long memberId, String oldPassword, String newPassword) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalStateException("會員不存在"));
        if (!passwordEncoder.matches(oldPassword, m.getPassword())) {
            throw new IllegalArgumentException("舊密碼不正確");
        }
        if (passwordEncoder.matches(newPassword, m.getPassword())) {
            throw new IllegalArgumentException("新密碼不可與舊密碼相同");
        }
        m.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(m);
    }
}
