package com.farm.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {

    @NotNull(message = "請指定商品")
    private Long productId;

    @NotNull
    @Min(value = 1, message = "數量至少 1")
    private Integer quantity;
}
