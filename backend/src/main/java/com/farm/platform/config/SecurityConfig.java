package com.farm.platform.config;

import com.farm.platform.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 靜態前端資源（多頁式靜態頁，所有 *.html / 靜態資產都放行）
                        .requestMatchers(HttpMethod.GET,
                                "/", "/*.html",
                                "/css/**", "/js/**", "/images/**",
                                "/favicon.ico", "/*.svg", "/*.png", "/*.jpg", "/*.webp", "/*.ico"
                        ).permitAll()
                        // 內部錯誤頁:任何未匹配 controller 的 404/500 都會 dispatch 到 /error,
                        // 若沒放行,Phase A 停用的 controller 會讓前端拿到 401 然後被踢登入
                        .requestMatchers("/error").permitAll()
                        // 公開端點：登入、註冊、商品瀏覽、團購瀏覽
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/group-buys", "/api/group-buys/*").permitAll()
                        // /api/farm-trips/orders/** 要登入(個人預約) - 必須放前面
                        .requestMatchers(HttpMethod.GET, "/api/farm-trips/orders/**").authenticated()
                        // /api/farm-trips/** GET 全部公開瀏覽
                        .requestMatchers(HttpMethod.GET, "/api/farm-trips/**").permitAll()
                        // 部落格 GET: /mine 要登入,其他公開
                        .requestMatchers(HttpMethod.GET, "/api/blogs/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/blogs/**").permitAll()
                        // 最新消息：GET 全部公開瀏覽
                        .requestMatchers(HttpMethod.GET, "/api/news", "/api/news/*").permitAll()
                        // 角色限制（範例）
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/farmer/**").hasRole("FARMER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint())
                        .accessDeniedHandler(jsonAccessDeniedHandler())
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** 未登入或 JWT 失效 → 回 401 JSON，前端據此自動登出 + 導去登入頁。 */
    @Bean
    public AuthenticationEntryPoint jsonAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":401,\"message\":\"登入逾期或尚未登入，請重新登入\"}");
        };
    }

    /** 已登入但無權限 → 回 403 JSON。 */
    @Bean
    public AccessDeniedHandler jsonAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":403,\"message\":\"無權限存取此資源\"}");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 前端 Vite 預設 5173
        cfg.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
