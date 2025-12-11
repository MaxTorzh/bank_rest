package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferRequest {
    @NotNull(message = "Source card ID cannot be null")
    private final Long fromCardId;

    @NotNull(message = "Destination card ID cannot be null")
    private final Long toCardId;

    @NotNull(message = "Amount cannot be null")
    private final Long amount;

    @NotNull(message = "Currency cannot be null")
    private final Currency currency;

    private final String description;
}
