package com.example.bankcards.controller;

import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.service.transfer.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getTotalOutgoingAmount_ShouldCallService() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        when(transferService.getTotalOutgoingAmountForPeriod(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5000L);

        mockMvc.perform(get("/api/transfers/my/statistics/outgoing")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("5000"))
                .andDo(result -> {
                    System.out.println("Status: " + result.getResponse().getStatus());
                    System.out.println("Response: " + result.getResponse().getContentAsString());
                });

        verify(transferService).getTotalOutgoingAmountForPeriod(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getAllTransfers_WithUserRole_ShouldReturnForbidden() throws Exception {
        // when & then
        mockMvc.perform(get("/api/transfers")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(result -> {
                    System.out.println("Status: " + result.getResponse().getStatus());
                    System.out.println("Expected 403, got: " + result.getResponse().getStatus());
                });
    }
}
