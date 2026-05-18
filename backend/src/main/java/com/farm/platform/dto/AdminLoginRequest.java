package com.farm.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 管理員專用登入(端點隱藏) */
@Data
public class AdminLoginRequest {
    @NotBlank @Email private String email;
    @NotBlank private String password;
}
