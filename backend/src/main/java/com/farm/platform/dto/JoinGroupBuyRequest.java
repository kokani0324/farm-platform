package com.farm.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class JoinGroupBuyRequest {

    @NotNull
    @Min(value = 1, message = "數量至少 1")
    private Integer quantity;

    @NotBlank(message = "請填收件人姓名")
    @Size(max = 50)
    private String recipientName;

    @NotBlank(message = "請填收件人電話")
    @Size(max = 20)
    private String recipientPhone;

    @NotBlank(message = "請填收件地址")
    @Size(max = 200)
    private String shippingAddress;

    @Size(max = 500)
    private String note;
}
