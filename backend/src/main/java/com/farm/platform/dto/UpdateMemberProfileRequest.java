package com.farm.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 會員編輯資料(Member 限定):Email 不可改 */
@Data
public class UpdateMemberProfileRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 50)
    private String nickname;

    @Pattern(regexp = "^$|^[0-9+\\-() ]{6,20}$", message = "電話格式不正確")
    private String phone;

    @Size(max = 100)
    private String address;
}
