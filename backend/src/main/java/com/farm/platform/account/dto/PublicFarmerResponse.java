package com.farm.platform.account.dto;

import com.farm.platform.account.entity.Farmer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class PublicFarmerResponse {
    private Long id;
    private String farmName;
    private String farmAddress;
    private String farmDesc;
    private String phone;
    private BigDecimal locLat;
    private BigDecimal locLong;
    private LocalDateTime createdAt;

    public static PublicFarmerResponse from(Farmer f) {
        return PublicFarmerResponse.builder()
                .id(f.getId())
                .farmName(f.getFarmName())
                .farmAddress(f.getFarmAddress())
                .farmDesc(f.getFarmDesc())
                .phone(f.getPhone())
                .locLat(f.getLocLat())
                .locLong(f.getLocLong())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
