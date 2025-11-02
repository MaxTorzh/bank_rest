package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TransferRequest {
    @NotNull(message = "Source card ID cannot be null")
    private final Long fromCardId;

    @NotNull(message = "Destination card ID cannot be null")
    private final Long toCardId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have up to 10 integer digits and 2 fraction digits")
    private final BigDecimal amount;

    private final String description;
}
