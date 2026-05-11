package com.farm.platform.dto;

import com.farm.platform.entity.GroupBuyOrder;
import com.farm.platform.entity.GroupBuyOrderStatus;
import com.farm.platform.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 團購整單回應。團主名下，包含全團彙整 + 各團員（participation）明細。
 */
@Data
@Builder
@AllArgsConstructor
public class GroupBuyOrderResponse {
    private Long id;
    private String orderNo;

    private Long groupBuyId;
    private String productName;
    private String productImageUrl;
    private String productUnit;

    private Long hostId;
    private String hostName;

    private Long farmerId;
    private String farmerName;

    private BigDecimal groupPrice;
    private Integer totalQuantity;
    private BigDecimal totalAmount;

    private GroupBuyOrderStatus status;
    private PaymentMethod paymentMethod;

    private LocalDateTime paidAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    /** 全團員明細（供團主、小農檢視） */
    private List<ParticipationResponse> participations;

    public static GroupBuyOrderResponse from(GroupBuyOrder o, List<ParticipationResponse> parts) {
        var gb = o.getGroupBuy();
        return GroupBuyOrderResponse.builder()
                .id(o.getId())
                .orderNo(o.getOrderNo())
                .groupBuyId(gb.getId())
                .productName(gb.getProduct().getName())
                .productImageUrl(gb.getProduct().getImageUrl())
                .productUnit(gb.getProduct().getUnit())
                .hostId(o.getHost().getId())
                .hostName(o.getHost().getName())
                .farmerId(o.getFarmer().getId())
                .farmerName(o.getFarmer().getName())
                .groupPrice(gb.getGroupPrice())
                .totalQuantity(o.getTotalQuantity())
                .totalAmount(o.getTotalAmount())
                .status(o.getStatus())
                .paymentMethod(o.getPaymentMethod())
                .paidAt(o.getPaidAt())
                .completedAt(o.getCompletedAt())
                .createdAt(o.getCreatedAt())
                .participations(parts)
                .build();
    }
}
