package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.config.TestConfig;
import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Currency;
import com.example.bankcards.security.SecurityService;
import com.example.bankcards.service.card.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(CardController.class)
@Import({SecurityConfig.class, TestConfig.class, TestSecurityConfig.class})
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    private CardDto createTestCardDto(Long id, Currency currency) {
        return CardDto.builder()
                .id(id)
                .cardNumber("**** **** **** 1234")
                .cardHolderName("Thomas A. Anderson")
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(1000L)
                .currency(currency)
                .userId(1L)
                .username("thomas")
                .createdAt(OffsetDateTime.now())
                .expired(false)
                .active(true)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_AsAdmin_ShouldCreateCard() throws Exception {
        CardDto responseDto = createTestCardDto(2L, Currency.USD);

        when(cardService.createCard(any(CardDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "cardHolderName": "Thomas A. Anderson",
                        "currency": "USD",
                        "balance": 0
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.cardHolderName").value("Thomas A. Anderson"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getExpiredCards_ShouldReturnExpiredCards() throws Exception {
        CardDto expiredCard = CardDto.builder()
                .id(3L)
                .cardHolderName("Expired Card")
                .currency(Currency.USD)
                .expired(true)
                .active(false)
                .build();

        when(cardService.getExpiredCards()).thenReturn(List.of(expiredCard));

        mockMvc.perform(get("/api/cards/my/expired"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].expired").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTotalBalance_ShouldReturnTotalBalance() throws Exception {
        when(cardService.getTotalUserBalance()).thenReturn(5000L);

        mockMvc.perform(get("/api/cards/balance/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("5000"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_AsAdmin_ShouldBlockCard() throws Exception {
        CardDto blockedCard = CardDto.builder()
                .id(1L)
                .status(CardStatus.BLOCKED)
                .currency(Currency.USD)
                .build();

        when(cardService.blockCard(1L)).thenReturn(blockedCard);

        mockMvc.perform(patch("/api/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_AsAdmin_ShouldActivateCard() throws Exception {
        CardDto activatedCard = CardDto.builder()
                .id(1L)
                .status(CardStatus.ACTIVE)
                .currency(Currency.USD)
                .build();

        when(cardService.activateCard(1L)).thenReturn(activatedCard);

        mockMvc.perform(patch("/api/cards/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_AsAdmin_ShouldDeleteCard() throws Exception {
        doNothing().when(cardService).deleteCard(1L);

        mockMvc.perform(delete("/api/cards/1"))
                .andExpect(status().isNoContent());

        verify(cardService).deleteCard(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCard_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}