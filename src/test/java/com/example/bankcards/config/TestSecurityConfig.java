package com.example.bankcards.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/users/me").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/users").hasRole("ADMIN")
                        .requestMatchers("/api/cards/my/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/cards/balance/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/cards/**").hasRole("ADMIN")
                        .requestMatchers("/api/transfers/my/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/transfers/**").hasRole("ADMIN")
                        .anyRequest().denyAll() // Все остальные запросы запрещены
                )
                .httpBasic(httpBasic -> {}); // Используем basic auth для простоты

        return http.build();
    }
}
