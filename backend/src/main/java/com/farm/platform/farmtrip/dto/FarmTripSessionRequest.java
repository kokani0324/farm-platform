package com.farm.platform.farmtrip.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 小農新增 / 編輯場次 */
@Data
public class FarmTripSessionRequest {

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal sessionPrice;

    @NotNull
    private LocalDateTime tripStart;

    @NotNull
    private LocalDateTime tripEnd;

    @NotNull
    private LocalDateTime bookStart;

    @NotNull
    private LocalDateTime bookEnd;
}
