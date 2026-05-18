package com.farm.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** 小農批次新增場次：選日期區間 (含頭尾) + 每日固定時段 + 報名截止規則 */
@Data
public class FarmTripSessionBatchRequest {

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    /** 每場活動的開始時刻（每天一場） */
    @NotNull
    private LocalTime dailyStartTime;

    /** 每場活動的結束時刻 */
    @NotNull
    private LocalTime dailyEndTime;

    /** 報名截止 = 活動開始日期前 N 天 24:00 之前。預設 3 */
    @NotNull
    @Min(0) @Max(60)
    private Integer bookEndDaysBefore = 3;

    /** 本場單價（省略則沿用活動展示價） */
    @DecimalMin(value = "0.00")
    private BigDecimal sessionPrice;

    /** 若某日已有未取消場次則跳過（避免重複建） */
    private Boolean skipExisting = true;
}
