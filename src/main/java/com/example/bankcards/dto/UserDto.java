package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class UserDto {
    private final Long id;
    private final String username;
    private final String email;
    private final Role role;
    private final OffsetDateTime createdAt;
    private final Integer cardsCount;
}
