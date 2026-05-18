package com.farm.platform.dto;

import com.farm.platform.entity.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 公開登入入口:userType 只允許 MEMBER / FARMER;ADMIN 走獨立隱藏 endpoint */
@Data
public class LoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private AccountType userType;
}
