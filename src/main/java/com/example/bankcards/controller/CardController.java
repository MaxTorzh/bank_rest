package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.card.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management", description = "APIs for managing bank cards")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get current user's cards")
    public ResponseEntity<Page<CardDto>> getUserCards(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<CardDto> cards = cardService.getUserCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/my/{cardId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and @cardService.isCardOwner(#cardId)")
    @Operation(summary = "Get card by ID")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long cardId) {
        CardDto card = cardService.getCardById(cardId);
        return ResponseEntity.ok(card);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new card (ADMIN only)")
    public ResponseEntity<CardDto> createCard(@RequestBody CardDto cardDTO) {
        CardDto createdCard = cardService.createCard(cardDTO);
        return ResponseEntity.ok(createdCard);
    }

    @PatchMapping("/{cardId}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN') and @cardService.isCardOwner(#cardId)")
    @Operation(summary = "Update card status")
    public ResponseEntity<CardDto> updateCardStatus(
            @PathVariable Long cardId,
            @RequestParam CardStatus status) {
        CardDto updatedCard = cardService.updateCardStatus(cardId, status);
        return ResponseEntity.ok(updatedCard);
    }

    @GetMapping("/my/expired")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get expired cards")
    public ResponseEntity<List<CardDto>> getExpiredCards() {
        List<CardDto> expiredCards = cardService.getExpiredCards();
        return ResponseEntity.ok(expiredCards);
    }

    @GetMapping("/balance/total")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get total balance of all active cards")
    public ResponseEntity<Long> getTotalBalance() {
        Long totalBalance = cardService.getTotalUserBalance();
        return ResponseEntity.ok(totalBalance);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all cards (ADMIN only)")
    public ResponseEntity<Page<CardDto>> getAllCards(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<CardDto> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get cards by user ID (ADMIN only)")
    public ResponseEntity<Page<CardDto>> getCardsByUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CardDto> cards = cardService.getCardsByUserId(userId, pageable);
        return ResponseEntity.ok(cards);
    }

    @PatchMapping("/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block card (ADMIN only)")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long cardId) {
        CardDto blockedCard = cardService.blockCard(cardId);
        return ResponseEntity.ok(blockedCard);
    }

    @PatchMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate card (ADMIN only)")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long cardId) {
        CardDto activatedCard = cardService.activateCard(cardId);
        return ResponseEntity.ok(activatedCard);
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete card (ADMIN only)")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}
