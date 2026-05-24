package com.farm.platform.security;

import com.farm.platform.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 拆分後的統一登入主體:由 JWT 內 userType + accountId 還原。
 * authorities 直接設成 ROLE_MEMBER / ROLE_FARMER / ROLE_ADMIN(配合既有 hasRole 慣例)。
 */
@Getter
@Builder
@AllArgsConstructor
public class AccountPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final AccountType type;
    private final boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + type.name()));
    }

    @Override public String getPassword() { return ""; } // JWT 流程不需要回傳密碼
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return enabled; }
}
