package com.farm.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "商品名稱必填")
    @Size(max = 100)
    private String name;

    @Size(max = 2000, message = "商品描述最多 2000 字")
    private String description;

    @NotNull(message = "請填寫單價")
    @DecimalMin(value = "0.01", message = "單價需大於 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @NotBlank(message = "計價單位必填，例如 斤、箱、公斤")
    @Size(max = 20)
    private String unit;

    @NotNull(message = "請填寫庫存")
    @Min(value = 0, message = "庫存不可小於 0")
    private Integer stock;

    @Size(max = 500)
    private String imageUrl;

    @Size(max = 100)
    private String origin;

    @Size(max = 50)
    private String shippingMethod;

    @NotNull
    private Boolean groupBuyEnabled = false;

    @NotNull(message = "請選擇商品分類")
    private Long categoryId;
}
