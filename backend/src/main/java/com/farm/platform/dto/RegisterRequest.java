package com.farm.platform.dto;

import com.farm.platform.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email 必填")
    @Email(message = "Email 格式錯誤")
    private String email;

    @NotBlank(message = "密碼必填")
    @Size(min = 6, max = 50, message = "密碼長度必須 6-50 字元")
    private String password;

    @NotBlank(message = "姓名必填")
    @Size(max = 50)
    private String name;

    @Size(max = 20)
    private String phone;

    @NotNull(message = "請選擇身分（CONSUMER 或 FARMER）")
    private Role role;
}
