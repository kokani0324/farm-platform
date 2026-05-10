package com.farm.platform.dto;

import com.farm.platform.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String name;
    /** 為向後相容保留,等同 activeRole */
    private Role role;
    /** 此 token 當下啟用的身份 */
    private Role activeRole;
    /** 此帳號擁有的所有身份(供前端決定是否顯示切換按鈕) */
    private Set<Role> roles;
}
