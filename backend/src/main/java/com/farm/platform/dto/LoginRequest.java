package com.farm.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email 必填")
    @Email
    private String email;

    @NotBlank(message = "密碼必填")
    private String password;
}
