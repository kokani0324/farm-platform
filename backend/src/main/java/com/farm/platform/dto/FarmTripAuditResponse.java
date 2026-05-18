package com.farm.platform.dto;

import com.farm.platform.entity.FarmTripAudit;
import com.farm.platform.entity.FarmTripAuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class FarmTripAuditResponse {
    private Long id;
    private Long farmTripId;
    private String farmTripTitle;
    private Long adminId;
    private String adminName;
    private FarmTripAuditStatus status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FarmTripAuditResponse from(FarmTripAudit a) {
        return FarmTripAuditResponse.builder()
                .id(a.getId())
                .farmTripId(a.getFarmTrip().getId())
                .farmTripTitle(a.getFarmTrip().getTitle())
                .adminId(a.getAdmin() != null ? a.getAdmin().getId() : null)
                .adminName(a.getAdmin() != null ? a.getAdmin().getName() : null)
                .status(a.getStatus())
                .reason(a.getReason())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
