package com.farm.platform.dto;

import com.farm.platform.entity.AccountStatus;
import com.farm.platform.entity.AccountType;
import com.farm.platform.entity.Farmer;
import com.farm.platform.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private AccountType type;
    private AccountStatus status;
    /** 對 FARMER 才有意義，否則 null */
    private Boolean certPassed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminUserResponse fromMember(Member m) {
        return AdminUserResponse.builder()
                .id(m.getId())
                .email(m.getEmail())
                .name(m.getName())
                .phone(m.getPhone())
                .type(AccountType.MEMBER)
                .status(m.getStatus())
                .certPassed(null)
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    public static AdminUserResponse fromFarmer(Farmer f) {
        return AdminUserResponse.builder()
                .id(f.getId())
                .email(f.getEmail())
                .name(f.getFarmName())
                .phone(f.getPhone())
                .type(AccountType.FARMER)
                .status(f.getStatus())
                .certPassed(f.getCertPassed())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
