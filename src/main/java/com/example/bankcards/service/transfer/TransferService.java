package com.example.bankcards.service.transfer;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;


public interface TransferService {

    TransferDto createTransfer(TransferRequest request);

    Page<TransferDto> getUserTransfers(Pageable pageable);

    Page<TransferDto> getOutgoingTransfers(Pageable pageable);

    Page<TransferDto> getIncomingTransfers(Pageable pageable);

    TransferDto getTransferById(Long transferId);

    Page<TransferDto> getAllTransfers(Pageable pageable);

    Page<TransferDto> getTransfersByStatus(TransferStatus status, Pageable pageable);

    Page<TransferDto> getTransfersByPeriod(OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable);

    TransferDto updateTransferStatus(Long transferId, TransferStatus status);

    void cancelTransfer(Long transferId);

    Long getTotalOutgoingAmountForPeriod(OffsetDateTime startDate, OffsetDateTime endDate);

    boolean isTransferParticipant(Long transferId);
}