package com.example.bankcards.service.card;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final SecurityService securityService;

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getUserCards(Pageable pageable) {
        User currentUser = securityService.getCurrentUser();
        log.debug("Fetching cards for user: {}", currentUser.getId());

        return cardRepository.findByUserId(currentUser.getId(), pageable)
                .map(cardMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCardById(Long cardId) {
        User currentUser = securityService.getCurrentUser();
        BankCard card = cardRepository.findByIdAndUserId(cardId, currentUser.getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        return cardMapper.toDTO(card);
    }

    @Override
    @Transactional
    public CardDto createCard(CardDto cardDTO) {
        User currentUser = securityService.getCurrentUser();

        if (cardRepository.existsByCardNumber(cardDTO.getCardNumber())) {
            throw new BadRequestException("Card with this number already exists");
        }

        BankCard card = cardMapper.toEntity(cardDTO);
        card.setUser(currentUser);

        BankCard savedCard = cardRepository.save(card);
        log.info("Card created successfully: {}", savedCard.getId());

        return cardMapper.toDTO(savedCard);
    }

    @Override
    @Transactional
    public CardDto updateCardStatus(Long cardId, CardStatus status) {
        User currentUser = securityService.getCurrentUser();
        BankCard card = cardRepository.findByIdAndUserId(cardId, currentUser.getId())
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        if (card.getStatus() == status) {
            throw new BadRequestException("Card already has status: " + status);
        }

        card.setStatus(status);
        BankCard updatedCard = cardRepository.save(card);
        log.info("Card status updated: {} -> {}", cardId, status);

        return cardMapper.toDTO(updatedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getExpiredCards() {
        User currentUser = securityService.getCurrentUser();
        List<BankCard> expiredCards = cardRepository.findExpiredCardsByUserId(currentUser.getId());

        return cardMapper.toDTOList(expiredCards);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getAllCards(Pageable pageable) {
        log.debug("Fetching all cards (ADMIN)");
        return cardRepository.findAll(pageable)
                .map(cardMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getCardsByUserId(Long userId, Pageable pageable) {
        return cardRepository.findByUserId(userId, pageable)
                .map(cardMapper::toDTO);
    }

    @Override
    @Transactional
    public CardDto blockCard(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        card.setStatus(CardStatus.BLOCKED);
        BankCard updatedCard = cardRepository.save(card);
        log.info("Card blocked by ADMIN: {}", cardId);

        return cardMapper.toDTO(updatedCard);
    }

    @Override
    @Transactional
    public CardDto activateCard(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        card.setStatus(CardStatus.ACTIVE);
        BankCard updatedCard = cardRepository.save(card);
        log.info("Card activated by ADMIN: {}", cardId);

        return cardMapper.toDTO(updatedCard);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        if (card.getBalance() > 0) {
            throw new BadRequestException("Cannot delete card with positive balance");
        }

        cardRepository.delete(card);
        log.info("Card deleted: {}", cardId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalUserBalance() {
        User currentUser = securityService.getCurrentUser();
        Long totalBalance = cardRepository.getTotalBalanceByUserId(currentUser.getId());

        return totalBalance != null ? totalBalance : 0L;
    }
}
