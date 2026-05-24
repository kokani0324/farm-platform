package com.farm.platform.account.dto;

import com.farm.platform.account.entity.AccountStatus;
import com.farm.platform.account.entity.AccountType;
import com.farm.platform.account.entity.Member;
import com.farm.platform.account.entity.MembershipLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 已登入會員的完整資料（profile.html 等頁面用） */
@Data
@Builder
@AllArgsConstructor
public class MemberMeResponse {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private String address;
    private AccountType type;
    private MembershipLevel level;
    private AccountStatus status;
    private Boolean isFarmer;
    private LocalDateTime createdAt;

    public static MemberMeResponse from(Member m) {
        return MemberMeResponse.builder()
                .id(m.getId())
                .email(m.getEmail())
                .name(m.getName())
                .nickname(m.getNickname())
                .phone(m.getPhone())
                .address(m.getAddress())
                .type(AccountType.MEMBER)
                .level(m.getLevel())
                .status(m.getStatus())
                .isFarmer(m.getIsFarmer())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
