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

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                // 💡 關鍵修改 1：針對綠界回傳的 API 路徑禁用 CSRF
                                // 綠界伺服器發送的 POST 請求不會帶有你的 CSRF Token，若不排除會導致 403 Forbidden
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/ecpay/callback").disable())

                                // 允許 CORS（配合 WebConfig.java 中的設定）
                                .cors(Customizer.withDefaults())

                                // 💡 核心設定：將 Session 策略設為 IF_REQUIRED，啟用 Session Cookie 來保持登入狀態
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                                .authorizeHttpRequests(auth -> auth
                                                // 💡 關鍵修改 2：確保綠界回傳路徑完全開放
                                                .requestMatchers("/api/ecpay/callback").permitAll()
                                                .requestMatchers("/api/ecpay/order-completed").permitAll()

                                                // 原有的白名單路徑
                                                .requestMatchers(
                                                                "/api/users/**",
                                                                "/api/users/login",
                                                                "/api/users/logout",
                                                                "/api/users/me",
                                                                "/api/products/**",
                                                                "/api/orders/**")
                                                .permitAll()

                                                // 其他所有請求都需要經過認證
                                                .anyRequest().authenticated());

                return http.build();
        }
}