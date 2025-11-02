package com.example.bankcards.dto;

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

    private final String description;
}
