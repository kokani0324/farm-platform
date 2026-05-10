package com.farm.platform.dto;

import com.farm.platform.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CheckoutRequest {

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

    @NotNull(message = "請選擇付款方式")
    private PaymentMethod paymentMethod;
}
