package com.farm.platform.dto;

import com.farm.platform.entity.AccountStatus;
import com.farm.platform.entity.Farmer;
import com.farm.platform.entity.FarmerCertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 管理員送審小農用的完整資訊。cert_file 本體不回傳（LONGBLOB 太大），改用 hasCertFile 旗標。 */
@Data
@Builder
@AllArgsConstructor
public class AdminFarmerReviewResponse {
    private Long id;
    private String email;
    private String farmName;
    private String farmAddress;
    private String farmDesc;
    private String phone;
    private FarmerCertType certType;
    private Boolean hasCertFile;
    private LocalDateTime uploadedAt;
    private Boolean certPassed;
    private AccountStatus status;
    private LocalDateTime createdAt;

    public static AdminFarmerReviewResponse from(Farmer f) {
        return AdminFarmerReviewResponse.builder()
                .id(f.getId())
                .email(f.getEmail())
                .farmName(f.getFarmName())
                .farmAddress(f.getFarmAddress())
                .farmDesc(f.getFarmDesc())
                .phone(f.getPhone())
                .certType(f.getCertType())
                .hasCertFile(f.getCertFile() != null && f.getCertFile().length > 0)
                .uploadedAt(f.getUploadedAt())
                .certPassed(f.getCertPassed())
                .status(f.getStatus())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
