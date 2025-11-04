package com.example.bankcards.service.transfer;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Currency;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.TransferNotFoundException;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final TransferMapper transferMapper;
    private final SecurityService securityService;

    @Override
    @Transactional
    public TransferDto createTransfer(TransferRequest request) {
        User currentUser = securityService.getCurrentUser();

        BankCard fromCard = cardRepository.findByIdAndUserId(request.getFromCardId(), currentUser.getId())
                .orElseThrow(() -> new CardNotFoundException("Source card not found or access denied"));

        BankCard toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new CardNotFoundException("Destination card not found"));

        validateTransfer(fromCard, toCard, request.getAmount(), request.getCurrency());

        Transfer transfer = transferMapper.toEntityFromRequest(request);
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setCurrency(request.getCurrency());

        return processTransfer(transfer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferDto> getUserTransfers(Pageable pageable) {
        User currentUser = securityService.getCurrentUser();
        return transferRepository.findByUserId(currentUser.getId(), pageable)
                .map(transferMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferDto> getOutgoingTransfers(Pageable pageable) {
        User currentUser = securityService.getCurrentUser();
        return transferRepository.findOutgoingTransfersByUserId(currentUser.getId(), pageable)
                .map(transferMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferDto> getIncomingTransfers(Pageable pageable) {
        User currentUser = securityService.getCurrentUser();
        return transferRepository.findIncomingTransfersByUserId(currentUser.getId(), pageable)
                .map(transferMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferDto getTransferById(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found with id: " + transferId));

        User currentUser = securityService.getCurrentUser();
        if (!isUserParticipant(transfer, currentUser)) {
            throw new TransferNotFoundException("Transfer not found with id: " + transferId);
        }

        return transferMapper.toDTO(transfer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferDto> getAllTransfers(Pageable pageable) {
        log.debug("Fetching all transfers (ADMIN)");
        return transferRepository.findAll(pageable)
                .map(transferMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferDto> getTransfersByStatus(TransferStatus status, Pageable pageable) {
        return transferRepository.findByStatus(status, pageable)
                .map(transferMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransferDto> getTransfersByPeriod(OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable) {
        return transferRepository.findByTransferDateBetween(startDate, endDate, pageable)
                .map(transferMapper::toDTO);
    }

    @Override
    @Transactional
    public TransferDto updateTransferStatus(Long transferId, TransferStatus status) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found with id: " + transferId));

        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            throw new BadRequestException("Cannot modify completed transfer");
        }

        transfer.setStatus(status);
        Transfer updatedTransfer = transferRepository.save(transfer);
        log.info("Transfer status updated: {} -> {}", transferId, status);

        return transferMapper.toDTO(updatedTransfer);
    }

    @Override
    @Transactional
    public void cancelTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found with id: " + transferId));

        if (transfer.getStatus() != TransferStatus.PENDING) {
            throw new BadRequestException("Only pending transfers can be cancelled");
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        transferRepository.save(transfer);
        log.info("Transfer cancelled: {}", transferId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalOutgoingAmountForPeriod(OffsetDateTime startDate, OffsetDateTime endDate) {
        User currentUser = securityService.getCurrentUser();
        Long totalAmount = transferRepository.getTotalOutgoingAmountByUserIdAndPeriod(
                currentUser.getId(), startDate, endDate);

        return totalAmount != null ? totalAmount : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTransferParticipant(Long transferId) {
        User currentUser = securityService.getCurrentUser();
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found with id: " + transferId));

        return isUserParticipant(transfer, currentUser);
    }

    private boolean isUserParticipant(Transfer transfer, User user) {
        return transfer.getFromCard().getUser().getId().equals(user.getId()) ||
                transfer.getToCard().getUser().getId().equals(user.getId());
    }

    private void validateTransfer(BankCard fromCard, BankCard toCard, Long amount, Currency currency) {
        if (fromCard.getId().equals(toCard.getId())) {
            throw new BadRequestException("Cannot transfer to the same card");
        }

        if (!fromCard.isActive()) {
            throw new BadRequestException("Source card is not active");
        }

        if (!toCard.isActive()) {
            throw new BadRequestException("Destination card is not active");
        }

        if (amount <= 0) {
            throw new BadRequestException("Amount must be positive");
        }

        if (fromCard.getBalance() < amount) {
            throw new BadRequestException("Insufficient funds");
        }

        if (amount > 1_000_000L) {
            throw new BadRequestException("Transfer amount exceeds limit");
        }

        if (fromCard.getCurrency() != currency) {
            throw new BadRequestException("Source card currency (" + fromCard.getCurrency() +
                    ") doesn't match transfer currency (" + currency + ")");
        }

        if (fromCard.getCurrency() != toCard.getCurrency()) {
            throw new BadRequestException("Currency mismatch: source card (" + fromCard.getCurrency() +
                    ") and destination card (" + toCard.getCurrency() + ")");
        }
    }

    private TransferDto processTransfer(Transfer transfer) {
        try {
            transfer.getFromCard().setBalance(transfer.getFromCard().getBalance() - transfer.getAmount());

            transfer.getToCard().setBalance(transfer.getToCard().getBalance() + transfer.getAmount());

            transfer.setStatus(TransferStatus.COMPLETED);

            Transfer savedTransfer = transferRepository.save(transfer);
            log.info("Transfer completed successfully: {} {} from card {} to card {}",
                    transfer.getAmount(), transfer.getCurrency(),
                    transfer.getFromCard().getId(), transfer.getToCard().getId());

            return transferMapper.toDTO(savedTransfer);

        } catch (Exception e) {
            transfer.setStatus(TransferStatus.FAILED);
            transferRepository.save(transfer);
            log.error("Transfer failed: {}", e.getMessage());
            throw new BadRequestException("Transfer failed: " + e.getMessage());
        }
    }
}
