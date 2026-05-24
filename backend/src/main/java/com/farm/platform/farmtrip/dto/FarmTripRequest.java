package com.farm.platform.farmtrip.dto;

import com.farm.platform.shop.entity.PricingMode;
import com.farm.platform.farmtrip.entity.TripType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/** 小農新增 / 編輯體驗活動（不含場次，場次走 session API） */
@Data
public class FarmTripRequest {

    @NotNull
    private TripType tripType;

    @NotNull
    private PricingMode pricingMode;

    @NotBlank
    @Size(max = 30)
    private String title;

    @Size(max = 500)
    private String intro;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 100)
    private String location;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal price;

    /** PER_PERSON 必填(>=1)；PER_WEIGHT 可為 null 表示無上限 */
    @Min(value = 1)
    private Integer capacityPerSession;
}
