package com.farm.platform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemRequest {

    @NotNull
    @Min(value = 1, message = "數量至少 1")
    private Integer quantity;
}
