package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private final String token;
    private final String type = "Bearer";
    private final Long userId;
    private final String username;
    private final Role role;
}
