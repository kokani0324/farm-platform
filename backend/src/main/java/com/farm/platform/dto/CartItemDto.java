package com.farm.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class CartItemDto {
    private Long productId;
    private String name;
    private String imageUrl;
    private String unit;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private BigDecimal subtotal;
    /** 商品是否仍可購買（下架/售完則 false，前端可提示移除） */
    private Boolean available;

    private Long farmerId;
    private String farmerName;
}
