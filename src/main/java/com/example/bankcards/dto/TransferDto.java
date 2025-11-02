package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.TransferStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TransferDto {
    private final Long id;
    private final Long fromCardId;
    private final String fromCardMaskedNumber;
    private final Long toCardId;
    private final String toCardMaskedNumber;
    private final Long amount;
    private final String description;
    private final LocalDateTime transferDate;
    private final TransferStatus status;
}
