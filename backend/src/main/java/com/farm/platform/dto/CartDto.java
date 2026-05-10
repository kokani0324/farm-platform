package com.farm.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CartDto {
    private List<CartItemDto> items;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
}
