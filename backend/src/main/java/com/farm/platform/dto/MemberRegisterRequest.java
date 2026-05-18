package com.farm.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 消費者(會員)註冊 */
@Data
public class MemberRegisterRequest {

    @NotBlank @Email
    @Size(max = 50)
    private String email;

    @NotBlank @Size(min = 6, max = 64)
    private String password;

    @NotBlank @Size(max = 50)
    private String name;

    @Size(max = 50)
    private String nickname;

    @Pattern(regexp = "^$|^[0-9+\\-() ]{6,20}$", message = "電話格式不正確")
    private String phone;

    @Size(max = 100)
    private String address;
}
