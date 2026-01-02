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
                http.csrf(csrf -> csrf
                                .ignoringRequestMatchers("/api/ecpay/**", "/api/users/**",
                                                "/api/products/**", "/api/orders/**"))
                                // 允許 CORS
                                .cors(Customizer.withDefaults())

                                // 💡 核心設定：將 Session 策略設為 IF_REQUIRED，啟用 Session Cookie 來保持登入狀態
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/error").permitAll()
                                                .requestMatchers("/api/ecpay/callback").permitAll()
                                                .requestMatchers("/api/ecpay/order-completed").permitAll()
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