package com.farm.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 編輯會員資料：只開放修改姓名與電話，Email 不可改。 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "姓名不可為空")
    @Size(max = 50, message = "姓名長度上限 50 字")
    private String name;

    @Pattern(regexp = "^$|^[0-9+\\-() ]{6,20}$", message = "電話格式不正確")
    private String phone;
}
