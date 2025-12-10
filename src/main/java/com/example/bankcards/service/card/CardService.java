package com.example.bankcards.service.card;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {

    Page<CardDto> getUserCards(Pageable pageable);

    CardDto getCardById(Long cardId);

    CardDto createCard(CardDto cardDTO);

    CardDto updateCardStatus(Long cardId, CardStatus status);

    List<CardDto> getExpiredCards();

    Page<CardDto> getAllCards(Pageable pageable);

    Page<CardDto> getCardsByUserId(Long userId, Pageable pageable);

    CardDto blockCard(Long cardId);

    CardDto activateCard(Long cardId);

    void deleteCard(Long cardId);

    Long getTotalUserBalance();
}
