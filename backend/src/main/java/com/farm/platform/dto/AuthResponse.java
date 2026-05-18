package com.farm.platform.dto;

import com.farm.platform.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** 統一登入回應(MEMBER / FARMER / ADMIN 共用) */
@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long accountId;
    private AccountType type;
    private String email;
    private String name;
}
