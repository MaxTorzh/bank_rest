package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.SecurityService;
import com.example.bankcards.service.card.CardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private CardServiceImpl cardService;

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        return user;
    }

    private BankCard createTestBankCard(Long id, CardStatus status) {
        BankCard card = new BankCard();
        card.setId(id);
        card.setCardNumber("1234567812345678");
        card.setCardHolderName("Thomas A. Anderson");
        card.setExpirationDate(LocalDate.now().plusYears(2));
        card.setStatus(status);
        card.setBalance(1000L);
        card.setUser(createTestUser());
        return card;
    }

    private CardDto createTestCardDto(Long id) {
        return CardDto.builder()
                .id(id)
                .cardNumber("1234567812345678")
                .cardHolderName("Thomas A. Anderson")
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(1000L)
                .userId(1L)
                .username("testuser")
                .createdAt(OffsetDateTime.now())
                .expired(false)
                .active(true)
                .build();
    }

    @Test
    void getTotalUserBalance_ShouldReturnBalance() {
        User user = createTestUser();
        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.getTotalBalanceByUserId(user.getId())).thenReturn(5000L);

        Long result = cardService.getTotalUserBalance();

        assertEquals(5000L, result);
    }

    @Test
    void getTotalUserBalance_WhenNoBalance_ShouldReturnZero() {
        User user = createTestUser();
        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.getTotalBalanceByUserId(user.getId())).thenReturn(null);

        Long result = cardService.getTotalUserBalance();

        assertEquals(0L, result);
    }

    @Test
    void getUserCards_ShouldReturnCards() {
        User user = createTestUser();
        Pageable pageable = PageRequest.of(0, 10);
        List<BankCard> cards = Arrays.asList(new BankCard(), new BankCard());
        Page<BankCard> cardsPage = new PageImpl<>(cards, pageable, 2);

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByUserId(user.getId(), pageable)).thenReturn(cardsPage);
        when(cardMapper.toDTO(any(BankCard.class))).thenReturn(createTestCardDto(1L));

        Page<CardDto> result = cardService.getUserCards(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void getCardById_WhenCardExists_ShouldReturnCard() {
        Long cardId = 1L;
        User user = createTestUser();
        BankCard card = createTestBankCard(cardId, CardStatus.ACTIVE);
        CardDto cardDto = createTestCardDto(cardId);

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByIdAndUserId(cardId, user.getId())).thenReturn(Optional.of(card));
        when(cardMapper.toDTO(card)).thenReturn(cardDto);

        CardDto result = cardService.getCardById(cardId);

        assertNotNull(result);
        assertEquals(cardId, result.getId());
        assertEquals("1234567812345678", result.getCardNumber());
    }

    @Test
    void getCardById_WhenCardNotExists_ShouldThrowException() {
        Long cardId = 999L;
        User user = createTestUser();

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByIdAndUserId(cardId, user.getId())).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(cardId));
    }

    @Test
    void createCard_WithValidData_ShouldCreateCard() {
        User user = createTestUser();
        CardDto cardDto = createTestCardDto(null);
        BankCard cardEntity = createTestBankCard(null, CardStatus.ACTIVE);
        BankCard savedCard = createTestBankCard(1L, CardStatus.ACTIVE);
        CardDto savedCardDto = createTestCardDto(1L);

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.existsByCardNumber(cardDto.getCardNumber())).thenReturn(false);
        when(cardMapper.toEntity(cardDto)).thenReturn(cardEntity);
        when(cardRepository.save(cardEntity)).thenReturn(savedCard);
        when(cardMapper.toDTO(savedCard)).thenReturn(savedCardDto);

        CardDto result = cardService.createCard(cardDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cardRepository).save(cardEntity);
        assertEquals(user, cardEntity.getUser());
    }

    @Test
    void createCard_WithDuplicateCardNumber_ShouldThrowException() {
        User user = createTestUser();
        CardDto cardDto = createTestCardDto(null);

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.existsByCardNumber(cardDto.getCardNumber())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> cardService.createCard(cardDto));
        verify(cardRepository, never()).save(any(BankCard.class));
    }

    @Test
    void updateCardStatus_WhenCardExists_ShouldUpdateStatus() {
        Long cardId = 1L;
        User user = createTestUser();
        CardStatus newStatus = CardStatus.BLOCKED;
        BankCard card = createTestBankCard(cardId, CardStatus.ACTIVE);
        CardDto updatedCardDto = CardDto.builder()
                .id(cardId)
                .cardNumber("1234567812345678")
                .cardHolderName("Thomas A. Anderson")
                .expirationDate(LocalDate.now().plusYears(2))
                .status(newStatus)
                .balance(1000L)
                .userId(1L)
                .username("testuser")
                .createdAt(OffsetDateTime.now())
                .expired(false)
                .active(false)
                .build();

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByIdAndUserId(cardId, user.getId())).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDTO(card)).thenReturn(updatedCardDto);

        CardDto result = cardService.updateCardStatus(cardId, newStatus);

        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        assertEquals(newStatus, card.getStatus());
    }

    @Test
    void updateCardStatus_WhenSameStatus_ShouldThrowException() {
        Long cardId = 1L;
        User user = createTestUser();
        CardStatus currentStatus = CardStatus.ACTIVE;
        BankCard card = createTestBankCard(cardId, currentStatus);

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findByIdAndUserId(cardId, user.getId())).thenReturn(Optional.of(card));

        assertThrows(BadRequestException.class, () -> cardService.updateCardStatus(cardId, currentStatus));
        verify(cardRepository, never()).save(any(BankCard.class));
    }

    @Test
    void getExpiredCards_ShouldReturnExpiredCards() {
        User user = createTestUser();
        List<BankCard> expiredCards = Arrays.asList(
                createTestBankCard(1L, CardStatus.ACTIVE),
                createTestBankCard(2L, CardStatus.BLOCKED)
        );

        when(securityService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findExpiredCardsByUserId(user.getId())).thenReturn(expiredCards);
        when(cardMapper.toDTOList(expiredCards)).thenReturn(Arrays.asList(
                createTestCardDto(1L),
                createTestCardDto(2L)
        ));

        List<CardDto> result = cardService.getExpiredCards();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllCards_ShouldReturnAllCards() {
        Pageable pageable = PageRequest.of(0, 10);
        List<BankCard> cards = Arrays.asList(
                createTestBankCard(1L, CardStatus.ACTIVE),
                createTestBankCard(2L, CardStatus.BLOCKED)
        );
        Page<BankCard> cardsPage = new PageImpl<>(cards, pageable, 2);

        when(cardRepository.findAll(pageable)).thenReturn(cardsPage);
        when(cardMapper.toDTO(any(BankCard.class))).thenReturn(createTestCardDto(1L));

        Page<CardDto> result = cardService.getAllCards(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }

    @Test
    void blockCard_WithAdminRole_ShouldBlockCard() {
        Long cardId = 1L;
        BankCard card = createTestBankCard(cardId, CardStatus.ACTIVE);
        CardDto blockedCardDto = CardDto.builder()
                .id(cardId)
                .cardNumber("1234567812345678")
                .cardHolderName("Thomas A. Anderson")
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.BLOCKED)
                .balance(1000L)
                .userId(1L)
                .username("testuser")
                .createdAt(OffsetDateTime.now())
                .expired(false)
                .active(false)
                .build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDTO(card)).thenReturn(blockedCardDto);

        CardDto result = cardService.blockCard(cardId);

        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED, result.getStatus());
        assertEquals(CardStatus.BLOCKED, card.getStatus());
    }

    @Test
    void activateCard_WithAdminRole_ShouldActivateCard() {
        Long cardId = 1L;
        BankCard card = createTestBankCard(cardId, CardStatus.BLOCKED);
        CardDto activatedCardDto = createTestCardDto(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDTO(card)).thenReturn(activatedCardDto);

        CardDto result = cardService.activateCard(cardId);

        assertNotNull(result);
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(CardStatus.ACTIVE, card.getStatus());
    }

    @Test
    void deleteCard_WithZeroBalance_ShouldDeleteCard() {
        Long cardId = 1L;
        BankCard card = createTestBankCard(cardId, CardStatus.ACTIVE);
        card.setBalance(0L);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.deleteCard(cardId);

        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCard_WithPositiveBalance_ShouldThrowException() {
        Long cardId = 1L;
        BankCard card = createTestBankCard(cardId, CardStatus.ACTIVE);
        card.setBalance(1000L);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThrows(BadRequestException.class, () -> cardService.deleteCard(cardId));
        verify(cardRepository, never()).delete(any(BankCard.class));
    }

    @Test
    void getCardsByUserId_ShouldReturnUserCards() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<BankCard> cards = Arrays.asList(
                createTestBankCard(1L, CardStatus.ACTIVE),
                createTestBankCard(2L, CardStatus.BLOCKED)
        );
        Page<BankCard> cardsPage = new PageImpl<>(cards, pageable, 2);

        when(cardRepository.findByUserId(userId, pageable)).thenReturn(cardsPage);
        when(cardMapper.toDTO(any(BankCard.class))).thenReturn(createTestCardDto(1L));

        Page<CardDto> result = cardService.getCardsByUserId(userId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }
}
