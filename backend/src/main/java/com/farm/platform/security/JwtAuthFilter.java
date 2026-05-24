package com.farm.platform.security;

import com.farm.platform.account.entity.AccountStatus;
import com.farm.platform.account.entity.AccountType;
import com.farm.platform.account.entity.Admin;
import com.farm.platform.account.entity.Farmer;
import com.farm.platform.account.entity.Member;
import com.farm.platform.account.repository.AdminRepository;
import com.farm.platform.account.repository.FarmerRepository;
import com.farm.platform.account.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Phase A 重構:依 JWT 內 type/aid 從對應 repository 載入 AccountPrincipal。
 * 三套帳號完全獨立,不再經 UserDetailsService。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final FarmerRepository farmerRepository;
    private final AdminRepository adminRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(PREFIX.length());
        if (!jwtUtil.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        AccountType type = jwtUtil.extractType(token);
        Long aid = jwtUtil.extractAccountId(token);
        if (type == null || aid == null
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        AccountPrincipal principal = loadPrincipal(type, aid);
        if (principal != null && principal.isEnabled()) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private AccountPrincipal loadPrincipal(AccountType type, Long aid) {
        return switch (type) {
            case MEMBER -> memberRepository.findById(aid)
                    .map(this::toPrincipal).orElse(null);
            case FARMER -> farmerRepository.findById(aid)
                    .map(this::toPrincipal).orElse(null);
            case ADMIN -> adminRepository.findById(aid)
                    .map(this::toPrincipal).orElse(null);
        };
    }

    private AccountPrincipal toPrincipal(Member m) {
        return AccountPrincipal.builder()
                .id(m.getId()).email(m.getEmail()).type(AccountType.MEMBER)
                .enabled(m.getStatus() == AccountStatus.NORMAL)
                .build();
    }
    private AccountPrincipal toPrincipal(Farmer f) {
        return AccountPrincipal.builder()
                .id(f.getId()).email(f.getEmail()).type(AccountType.FARMER)
                .enabled(Boolean.TRUE.equals(f.getCertPassed()) && f.getStatus() == AccountStatus.NORMAL)
                .build();
    }
    private AccountPrincipal toPrincipal(Admin a) {
        return AccountPrincipal.builder()
                .id(a.getId()).email(a.getEmail()).type(AccountType.ADMIN)
                .enabled(a.getStatus() == AccountStatus.NORMAL)
                .build();
    }
}
