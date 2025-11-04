package com.example.bankcards.service;

import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.TransferNotFoundException;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.security.SecurityService;
import com.example.bankcards.service.transfer.TransferServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransferMapper transferMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private TransferServiceImpl transferService;

    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        return user;
    }

    private BankCard createTestCard(Long id, User user, Long balance, boolean active) {
        BankCard card = new BankCard();
        card.setId(id);
        card.setCardNumber("1234567812345678");
        card.setCardHolderName("John Doe");
        card.setBalance(balance);
        card.setUser(user);
        card.setStatus(active ? CardStatus.ACTIVE : CardStatus.BLOCKED);
        return card;
    }

    private Transfer createTestTransfer(Long id, BankCard fromCard, BankCard toCard, Long amount) {
        Transfer transfer = new Transfer();
        transfer.setId(id);
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(amount);
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setTransferDate(LocalDateTime.now());
        return transfer;
    }

    private TransferRequest createTransferRequest(Long fromCardId, Long toCardId, Long amount) {
        return TransferRequest.builder()
                .fromCardId(fromCardId)
                .toCardId(toCardId)
                .amount(amount)
                .description("Test transfer")
                .build();
    }

    @Test
    void createTransfer_WhenSameCard_ShouldThrowException() {
        User user = createTestUser(1L);
        TransferRequest request = createTransferRequest(1L, 1L, 1000L);

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(createTestCard(1L, user, 5000L, true)));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(createTestCard(1L, user, 5000L, true)));

        assertThrows(BadRequestException.class, () -> transferService.createTransfer(request));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void createTransfer_WhenFromCardNotActive_ShouldThrowException() {
        User user = createTestUser(1L);
        BankCard fromCard = createTestCard(1L, user, 5000L, false);
        BankCard toCard = createTestCard(2L, createTestUser(2L), 1000L, true);
        TransferRequest request = createTransferRequest(1L, 2L, 1000L);

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(BadRequestException.class, () -> transferService.createTransfer(request));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void createTransfer_WhenFromCardNotFound_ShouldThrowException() {
        User user = createTestUser(1L);
        TransferRequest request = createTransferRequest(1L, 2L, 1000L);

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> transferService.createTransfer(request));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void createTransfer_WhenToCardNotFound_ShouldThrowException() {
        User user = createTestUser(1L);
        BankCard fromCard = createTestCard(1L, user, 5000L, true);
        TransferRequest request = createTransferRequest(1L, 2L, 1000L);

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> transferService.createTransfer(request));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void getTransferById_WhenTransferExistsAndUserIsParticipant_ShouldReturnTransfer() {
        User user = createTestUser(1L);
        BankCard fromCard = createTestCard(1L, user, 5000L, true);
        Transfer transfer = createTestTransfer(1L, fromCard, createTestCard(2L, createTestUser(2L), 1000L, true), 1000L);
        TransferDto transferDto = TransferDto.builder().id(1L).amount(1000L).build();

        when(securityService.getCurrentUser()).thenReturn(user);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(transferMapper.toDTO(transfer)).thenReturn(transferDto);

        TransferDto result = transferService.getTransferById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(transferRepository).findById(1L);
    }

    @Test
    void getTransferById_WhenUserNotParticipant_ShouldThrowException() {
        User user = createTestUser(1L);
        User otherUser = createTestUser(2L);
        BankCard fromCard = createTestCard(1L, otherUser, 5000L, true);
        BankCard toCard = createTestCard(2L, otherUser, 1000L, true);
        Transfer transfer = createTestTransfer(1L, fromCard, toCard, 1000L);

        when(securityService.getCurrentUser()).thenReturn(user);
        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThrows(TransferNotFoundException.class, () -> transferService.getTransferById(1L));
    }

    @Test
    void getUserTransfers_ShouldReturnUserTransfers() {
        User user = createTestUser(1L);
        Pageable pageable = PageRequest.of(0, 10);
        Transfer transfer = createTestTransfer(1L, createTestCard(1L, user, 5000L, true),
                createTestCard(2L, createTestUser(2L), 1000L, true), 1000L);
        Page<Transfer> transfersPage = new PageImpl<>(Collections.singletonList(transfer));

        when(securityService.getCurrentUser()).thenReturn(user);
        when(transferRepository.findByUserId(1L, pageable)).thenReturn(transfersPage);
        when(transferMapper.toDTO(any(Transfer.class))).thenReturn(TransferDto.builder().id(1L).build());

        Page<TransferDto> result = transferService.getUserTransfers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(transferRepository).findByUserId(1L, pageable);
    }

    @Test
    void getTotalOutgoingAmountForPeriod_ShouldReturnTotalAmount() {
        User user = createTestUser(1L);
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(securityService.getCurrentUser()).thenReturn(user);
        when(transferRepository.getTotalOutgoingAmountByUserIdAndPeriod(1L, startDate, endDate)).thenReturn(5000L);

        Long result = transferService.getTotalOutgoingAmountForPeriod(startDate, endDate);

        assertEquals(5000L, result);
        verify(transferRepository).getTotalOutgoingAmountByUserIdAndPeriod(1L, startDate, endDate);
    }

    @Test
    void getTotalOutgoingAmountForPeriod_WhenNoTransfers_ShouldReturnZero() {
        User user = createTestUser(1L);
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(securityService.getCurrentUser()).thenReturn(user);
        when(transferRepository.getTotalOutgoingAmountByUserIdAndPeriod(1L, startDate, endDate)).thenReturn(null);

        Long result = transferService.getTotalOutgoingAmountForPeriod(startDate, endDate);

        assertEquals(0L, result);
    }

    @Test
    void updateTransferStatus_WhenTransferExists_ShouldUpdateStatus() {
        Transfer transfer = createTestTransfer(1L, createTestCard(1L, createTestUser(1L), 5000L, true),
                createTestCard(2L, createTestUser(2L), 1000L, true), 1000L);
        transfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(transfer)).thenReturn(transfer);
        when(transferMapper.toDTO(transfer)).thenReturn(TransferDto.builder().id(1L).status(TransferStatus.COMPLETED).build());

        TransferDto result = transferService.updateTransferStatus(1L, TransferStatus.COMPLETED);

        assertNotNull(result);
        assertEquals(TransferStatus.COMPLETED, result.getStatus());
        assertEquals(TransferStatus.COMPLETED, transfer.getStatus());
        verify(transferRepository).save(transfer);
    }

    @Test
    void updateTransferStatus_WhenTransferCompleted_ShouldThrowException() {
        Transfer transfer = createTestTransfer(1L, createTestCard(1L, createTestUser(1L), 5000L, true),
                createTestCard(2L, createTestUser(2L), 1000L, true), 1000L);
        transfer.setStatus(TransferStatus.COMPLETED);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThrows(BadRequestException.class, () -> transferService.updateTransferStatus(1L, TransferStatus.CANCELLED));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void cancelTransfer_WhenPendingTransfer_ShouldCancel() {
        Transfer transfer = createTestTransfer(1L, createTestCard(1L, createTestUser(1L), 5000L, true),
                createTestCard(2L, createTestUser(2L), 1000L, true), 1000L);
        transfer.setStatus(TransferStatus.PENDING);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(transfer)).thenReturn(transfer);

        transferService.cancelTransfer(1L);

        assertEquals(TransferStatus.CANCELLED, transfer.getStatus());
        verify(transferRepository).save(transfer);
    }

    @Test
    void cancelTransfer_WhenNotPendingTransfer_ShouldThrowException() {
        Transfer transfer = createTestTransfer(1L, createTestCard(1L, createTestUser(1L), 5000L, true),
                createTestCard(2L, createTestUser(2L), 1000L, true), 1000L);
        transfer.setStatus(TransferStatus.COMPLETED);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        assertThrows(BadRequestException.class, () -> transferService.cancelTransfer(1L));
        verify(transferRepository, never()).save(any(Transfer.class));
    }
}