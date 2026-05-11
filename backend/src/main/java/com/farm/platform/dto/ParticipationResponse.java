package com.farm.platform.dto;

import com.farm.platform.entity.GroupBuyParticipation;
import com.farm.platform.entity.ParticipationStatus;
import com.farm.platform.entity.PaymentMethod;
import com.farm.platform.entity.PaymentStatus;
import com.farm.platform.entity.ReceiptStatus;
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

    private Long userId;
    private String userName;

    private Boolean isHost;
    private Integer quantity;
    private BigDecimal subtotal;

    private String recipientName;
    private String recipientPhone;
    private String shippingZipcode;
    private String shippingCity;
    private String shippingDist;
    private String shippingDetail;
    private String fullAddress;
    private String note;

    private ParticipationStatus status;
    private LocalDateTime joinedAt;

    /** 付款追蹤 */
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;

    /** 出貨 / 收貨追蹤 */
    private ReceiptStatus receiptStatus;
    private LocalDateTime shippedAt;
    private LocalDateTime receiptDatetime;

    /** 成團後對應的團購整單 id（屬團主，若已成團） */
    private Long groupBuyOrderId;
    private String groupBuyOrderNo;

    public static ParticipationResponse from(GroupBuyParticipation p) {
        var gb = p.getGroupBuy();
        return ParticipationResponse.builder()
                .id(p.getId())
                .groupBuyId(gb.getId())
                .productName(gb.getProduct().getName())
                .productImageUrl(gb.getProduct().getImageUrl())
                .productUnit(gb.getProduct().getUnit())
                .farmerName(gb.getFarmer().getName())
                .userId(p.getUser().getId())
                .userName(p.getUser().getName())
                .isHost(p.getIsHost())
                .quantity(p.getQuantity())
                .subtotal(p.getSubtotal())
                .recipientName(p.getRecipientName())
                .recipientPhone(p.getRecipientPhone())
                .shippingZipcode(p.getShippingZipcode())
                .shippingCity(p.getShippingCity())
                .shippingDist(p.getShippingDist())
                .shippingDetail(p.getShippingDetail())
                .fullAddress(p.getFullAddress())
                .note(p.getNote())
                .status(p.getStatus())
                .joinedAt(p.getJoinedAt())
                .paymentStatus(p.getPaymentStatus())
                .paymentMethod(p.getPaymentMethod())
                .paidAt(p.getPaidAt())
                .refundedAt(p.getRefundedAt())
                .receiptStatus(p.getReceiptStatus())
                .shippedAt(p.getShippedAt())
                .receiptDatetime(p.getReceiptDatetime())
                .groupBuyOrderId(p.getGroupBuyOrder() != null ? p.getGroupBuyOrder().getId() : null)
                .groupBuyOrderNo(p.getGroupBuyOrder() != null ? p.getGroupBuyOrder().getOrderNo() : null)
                .build();
    }
}
