package com.farm.platform.dto;

import com.farm.platform.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SwitchRoleRequest {
    @NotNull(message = "請指定要切換的身份")
    private Role role;
}
