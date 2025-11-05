package com.example.bankcards.config;

import com.example.bankcards.util.jwt.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        var user = User.builder()
                .username("testuser")
                .password("password")
                .roles("USER")
                .build();

        var admin = User.builder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        JwtUtil jwtUtilMock = mock(JwtUtil.class);

        when(jwtUtilMock.generateToken(any())).thenReturn("mock-jwt-token");
        when(jwtUtilMock.extractUsername(any())).thenReturn("testuser");
        when(jwtUtilMock.isTokenValid(any())).thenReturn(true);
        when(jwtUtilMock.validateToken(any(), any())).thenReturn(true);

        return jwtUtilMock;
    }
}
