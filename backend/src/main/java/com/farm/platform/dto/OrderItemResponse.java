package com.farm.platform.dto;

import com.farm.platform.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private String unit;
    private String imageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;

    public static OrderItemResponse from(OrderItem i) {
        return OrderItemResponse.builder()
                .productId(i.getProduct() != null ? i.getProduct().getId() : null)
                .productName(i.getProductName())
                .unit(i.getUnit())
                .imageUrl(i.getImageUrl())
                .unitPrice(i.getUnitPrice())
                .quantity(i.getQuantity())
                .subtotal(i.getSubtotal())
                .build();
    }
}
