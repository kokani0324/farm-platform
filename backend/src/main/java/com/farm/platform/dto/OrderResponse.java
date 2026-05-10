package com.farm.platform.dto;

import com.farm.platform.entity.Order;
import com.farm.platform.entity.OrderStatus;
import com.farm.platform.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNo;

    private Long farmerId;
    private String farmerName;

    private Long consumerId;
    private String consumerName;

    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;

    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String note;

    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 若此單來自團購,標示 groupBuyId(前端可加 badge);一般訂單為 null */
    private Long groupBuyId;

    private List<OrderItemResponse> items;

    public static OrderResponse from(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .orderNo(o.getOrderNo())
                .farmerId(o.getFarmer().getId())
                .farmerName(o.getFarmer().getName())
                .consumerId(o.getConsumer().getId())
                .consumerName(o.getConsumer().getName())
                .status(o.getStatus())
                .paymentMethod(o.getPaymentMethod())
                .totalAmount(o.getTotalAmount())
                .recipientName(o.getRecipientName())
                .recipientPhone(o.getRecipientPhone())
                .shippingAddress(o.getShippingAddress())
                .note(o.getNote())
                .paidAt(o.getPaidAt())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .groupBuyId(o.getGroupBuy() != null ? o.getGroupBuy().getId() : null)
                .items(o.getItems().stream().map(OrderItemResponse::from).toList())
                .build();
    }
}
