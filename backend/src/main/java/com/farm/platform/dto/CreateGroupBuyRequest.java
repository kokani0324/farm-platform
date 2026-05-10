package com.farm.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateGroupBuyRequest {

    @NotNull(message = "請指定商品")
    private Long productId;

    @NotNull
    @Min(value = 2, message = "目標數量至少 2")
    private Integer targetQuantity;

    @NotNull
    @DecimalMin(value = "1.00", message = "團購價需大於 0")
    private BigDecimal groupPrice;

    @NotNull(message = "請設定開團時間")
    private LocalDateTime openDate;

    @NotNull(message = "請設定截止時間")
    @Future(message = "截止時間必須在未來")
    private LocalDateTime deadlineDate;

    @Size(max = 500)
    private String message;
}
