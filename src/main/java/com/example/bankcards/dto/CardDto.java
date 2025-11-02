package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CardDto {
    private final Long id;
    private final String cardNumber;
    private final String cardHolderName;
    private final LocalDate expirationDate;
    private final CardStatus status;
    private final Long balance;
    private final Long userId;
    private final String username;
    private final LocalDateTime createdAt;
    private final boolean expired;
    private final boolean active;
}
