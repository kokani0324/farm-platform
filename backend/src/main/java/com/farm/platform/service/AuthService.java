package com.farm.platform.service;

import com.farm.platform.dto.AuthResponse;
import com.farm.platform.dto.LoginRequest;
import com.farm.platform.dto.RegisterRequest;
import com.farm.platform.entity.Role;
import com.farm.platform.entity.User;
import com.farm.platform.repository.UserRepository;
import com.farm.platform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (req.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("不允許註冊管理員帳號");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email 已被註冊");
        }

        // 註冊時:CONSUMER → {CONSUMER};FARMER → {CONSUMER, FARMER}(自動同時擁有消費者身份)
        Set<Role> roles = new HashSet<>();
        roles.add(Role.CONSUMER);
        if (req.getRole() == Role.FARMER) roles.add(Role.FARMER);

        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .phone(req.getPhone())
                .role(req.getRole())            // 主要註冊身份
                .roles(roles)
                // 小農預設停用、需管理員審核;消費者直接啟用
                .enabled(req.getRole() != Role.FARMER)
                .build();

        User saved = userRepository.save(user);

        // active role 預設 = 主要註冊身份
        Role active = req.getRole();
        String token = jwtUtil.generateToken(saved.getEmail(), active.name());
        return buildResponse(saved, token, active);
    }

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("使用者不存在"));

        // 預設 active role = 主要註冊身份(若該身份還在 roles 集合中)
        Role active = user.hasRole(user.getRole()) ? user.getRole() : user.getRoles().iterator().next();
        String token = jwtUtil.generateToken(user.getEmail(), active.name());
        return buildResponse(user, token, active);
    }

    /** 切換身份:重簽 JWT 把 active role 換成新的(需此使用者真的擁有該角色) */
    public AuthResponse switchRole(String email, Role newRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("使用者不存在"));
        if (!user.hasRole(newRole)) {
            throw new AccessDeniedException("您沒有 " + newRole + " 身份");
        }
        String token = jwtUtil.generateToken(user.getEmail(), newRole.name());
        return buildResponse(user, token, newRole);
    }

    private AuthResponse buildResponse(User user, String token, Role activeRole) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(activeRole)               // 為向後相容,role = activeRole
                .activeRole(activeRole)
                .roles(user.getRoles())
                .build();
    }
}
