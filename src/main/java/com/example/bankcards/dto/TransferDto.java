package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.Currency;
import com.example.bankcards.entity.enums.TransferStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class TransferDto {
    private final Long id;
    private final Long fromCardId;
    private final String fromCardMaskedNumber;
    private final Long toCardId;
    private final String toCardMaskedNumber;
    private final Long amount;
    private final Currency currency;
    private final String description;
    private final OffsetDateTime transferDate;
    private final TransferStatus status;
}
