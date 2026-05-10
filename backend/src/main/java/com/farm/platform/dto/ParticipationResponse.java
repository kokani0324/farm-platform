package com.farm.platform.dto;

import com.farm.platform.entity.GroupBuyParticipation;
import com.farm.platform.entity.ParticipationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ParticipationResponse {
    private Long id;
    private Long groupBuyId;
    private String productName;
    private String productImageUrl;
    private String productUnit;
    private String farmerName;

    private Boolean isHost;
    private Integer quantity;
    private BigDecimal subtotal;

    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String note;

    private ParticipationStatus status;
    private LocalDateTime joinedAt;

    /** 成團後對應的訂單 id(若有) */
    private Long orderId;

    public static ParticipationResponse from(GroupBuyParticipation p) {
        var gb = p.getGroupBuy();
        return ParticipationResponse.builder()
                .id(p.getId())
                .groupBuyId(gb.getId())
                .productName(gb.getProduct().getName())
                .productImageUrl(gb.getProduct().getImageUrl())
                .productUnit(gb.getProduct().getUnit())
                .farmerName(gb.getFarmer().getName())
                .isHost(p.getIsHost())
                .quantity(p.getQuantity())
                .subtotal(p.getSubtotal())
                .recipientName(p.getRecipientName())
                .recipientPhone(p.getRecipientPhone())
                .shippingAddress(p.getShippingAddress())
                .note(p.getNote())
                .status(p.getStatus())
                .joinedAt(p.getJoinedAt())
                .orderId(p.getOrder() != null ? p.getOrder().getId() : null)
                .build();
    }
}
