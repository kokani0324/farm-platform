package com.farm.platform.dto;

import com.farm.platform.entity.Role;
import com.farm.platform.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private Role primaryRole;
    private Set<Role> roles;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminUserResponse from(User u) {
        return AdminUserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .name(u.getName())
                .phone(u.getPhone())
                .primaryRole(u.getRole())
                .roles(u.getRoles() == null ? Set.of() : u.getRoles().stream().collect(Collectors.toSet()))
                .enabled(u.getEnabled())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
