package com.farm.platform.dto;

import com.farm.platform.entity.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class JoinGroupBuyRequest {

    @NotNull
    @Min(value = 1, message = "數量至少 1")
    private Integer quantity;

    @NotNull(message = "請選擇付款方式")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "請填收件人姓名")
    @Size(max = 50)
    private String recipientName;

    @NotBlank(message = "請填收件人電話")
    @Size(max = 20)
    private String recipientPhone;

    @NotBlank(message = "請填郵遞區號")
    @Size(max = 10)
    private String shippingZipcode;

    @NotBlank(message = "請選擇縣市")
    @Size(max = 20)
    private String shippingCity;

    @NotBlank(message = "請選擇鄉鎮區")
    @Size(max = 20)
    private String shippingDist;

    @NotBlank(message = "請填詳細地址")
    @Size(max = 200)
    private String shippingDetail;

    @Size(max = 500)
    private String note;
}
