package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.config.TestConfig;
import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.dto.TransferDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.enums.Currency;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.security.SecurityService;
import com.example.bankcards.service.transfer.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@Import({SecurityConfig.class, TestConfig.class, TestSecurityConfig.class})
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    private TransferDto createTestTransferDto(Long id, Currency currency) {
        return TransferDto.builder()
                .id(id)
                .fromCardId(1L)
                .toCardId(2L)
                .fromCardMaskedNumber("**** **** **** 1234")
                .toCardMaskedNumber("**** **** **** 5678")
                .amount(100L)
                .currency(currency)
                .description("Test transfer")
                .transferDate(OffsetDateTime.now())
                .status(TransferStatus.COMPLETED)
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void createTransfer_ShouldCreateTransfer() throws Exception {
        TransferDto response = createTestTransferDto(1L, Currency.USD);

        when(transferService.createTransfer(any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "fromCardId": 1,
                        "toCardId": 2,
                        "amount": 100,
                        "currency": "USD",
                        "description": "Test transfer"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.fromCardMaskedNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.toCardMaskedNumber").value("**** **** **** 5678"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTotalOutgoingAmount_ShouldReturnTotalAmount() throws Exception {
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(30);
        OffsetDateTime endDate = OffsetDateTime.now();

        when(transferService.getTotalOutgoingAmountForPeriod(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(5000L);

        mockMvc.perform(get("/api/transfers/my/statistics/outgoing")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("5000"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTransferStatus_AsAdmin_ShouldUpdateStatus() throws Exception {
        TransferDto updatedTransfer = TransferDto.builder()
                .id(1L)
                .status(TransferStatus.CANCELLED)
                .currency(Currency.USD)
                .build();

        when(transferService.updateTransferStatus(1L, TransferStatus.CANCELLED))
                .thenReturn(updatedTransfer);

        mockMvc.perform(patch("/api/transfers/1/status")
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelTransfer_AsAdmin_ShouldCancelTransfer() throws Exception {
        doNothing().when(transferService).cancelTransfer(1L);

        mockMvc.perform(delete("/api/transfers/1"))
                .andExpect(status().isNoContent());

        verify(transferService).cancelTransfer(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllTransfers_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isForbidden());
    }
}
