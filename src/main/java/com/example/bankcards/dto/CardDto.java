package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Currency;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Builder
public class CardDto {
    private final Long id;
    private final String cardNumber;
    private final String cardHolderName;
    private final LocalDate expirationDate;
    private final CardStatus status;
    private final Long balance;
    private final Currency currency;
    private final Long userId;
    private final String username;
    private final OffsetDateTime createdAt;
    private final boolean expired;
    private final boolean active;
}
