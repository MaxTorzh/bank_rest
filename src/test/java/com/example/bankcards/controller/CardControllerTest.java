package com.example.bankcards.controller;

import com.example.bankcards.service.card.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getExpiredCards_ShouldReturnEmptyList() throws Exception {
        when(cardService.getExpiredCards()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/cards/my/expired")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0))
                .andDo(result -> {
                    System.out.println("Response: " + result.getResponse().getContentAsString());
                    System.out.println("Content-Type: " + result.getResponse().getContentType());
                });

        verify(cardService).getExpiredCards();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getTotalBalance_ShouldReturnTotalBalance() throws Exception {
        when(cardService.getTotalUserBalance()).thenReturn(5000L);

        mockMvc.perform(get("/api/cards/balance/total")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("5000"))
                .andDo(result -> {
                    System.out.println("Response: " + result.getResponse().getContentAsString());
                    System.out.println("Content-Type: " + result.getResponse().getContentType());
                });

        verify(cardService).getTotalUserBalance();
    }
}
