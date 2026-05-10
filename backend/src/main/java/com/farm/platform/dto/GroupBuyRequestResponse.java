package com.farm.platform.dto;

import com.farm.platform.entity.GroupBuyRequest;
import com.farm.platform.entity.GroupBuyRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class GroupBuyRequestResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private String productUnit;
    private BigDecimal productPrice;     // 原價

    private Long initiatorId;
    private String initiatorName;

    private Long farmerId;
    private String farmerName;

    private Integer targetQuantity;
    private BigDecimal groupPrice;
    private LocalDateTime openDate;
    private LocalDateTime deadlineDate;

    private GroupBuyRequestStatus status;
    private String rejectReason;
    private String message;

    private LocalDateTime requestedAt;
    private LocalDateTime repliedAt;

    /** 通過後對應的團購活動 id(若有) */
    private Long groupBuyId;

    public static GroupBuyRequestResponse from(GroupBuyRequest r, Long groupBuyId) {
        return GroupBuyRequestResponse.builder()
                .id(r.getId())
                .productId(r.getProduct().getId())
                .productName(r.getProduct().getName())
                .productImageUrl(r.getProduct().getImageUrl())
                .productUnit(r.getProduct().getUnit())
                .productPrice(r.getProduct().getPrice())
                .initiatorId(r.getInitiator().getId())
                .initiatorName(r.getInitiator().getName())
                .farmerId(r.getFarmer().getId())
                .farmerName(r.getFarmer().getName())
                .targetQuantity(r.getTargetQuantity())
                .groupPrice(r.getGroupPrice())
                .openDate(r.getOpenDate())
                .deadlineDate(r.getDeadlineDate())
                .status(r.getStatus())
                .rejectReason(r.getRejectReason())
                .message(r.getMessage())
                .requestedAt(r.getRequestedAt())
                .repliedAt(r.getRepliedAt())
                .groupBuyId(groupBuyId)
                .build();
    }
}
