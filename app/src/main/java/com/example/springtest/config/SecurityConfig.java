// SecurityConfig.java

package com.example.springtest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 💡 由於您選擇不使用密碼保護，我們這裡不配置 PasswordEncoder。

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 禁用 CSRF
                .csrf(csrf -> csrf.disable())
                // 允許 CORS
                .cors(Customizer.withDefaults())

                // 啟用 HTTP Basic 認證
                // .httpBasic(Customizer.withDefaults())

                // 💡 核心：將 Session 策略設為 IF_REQUIRED，啟用 Session Cookie 來保持登入
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authorizeHttpRequests(auth -> auth
                        // 允許註冊、產品列表、登入、登出和獲取當前用戶公開存取
                        .requestMatchers("/api/users", "/api/products/**", "/api/users/login", "/api/users/logout",
                                "/api/users/me", "/api/orders/**")
                        .permitAll()

                        // 其他所有請求都需要經過認證 (例如 /api/orders)
                        .anyRequest().authenticated())
                .build();
    }
}