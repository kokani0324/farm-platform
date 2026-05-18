package com.farm.platform.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/** 小農補登 PER_WEIGHT 訂單的實際採收重量；total = actualWeight × unitPrice */
@Data
public class FarmTripCompleteRequest {

    /** PER_WEIGHT 必填(>0)；PER_PERSON 可省略，直接以下單金額完成 */
    @DecimalMin(value = "0.00")
    private BigDecimal actualWeight;
}
