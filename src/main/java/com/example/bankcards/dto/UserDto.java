package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserDto {
    private final Long id;
    private final String name;
    private final String email;
    private final Role role;
    private final LocalDateTime createdAt;
    private final Integer cardsCount;
}
