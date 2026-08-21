package com.vois.simpleewalletsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.response.TransactionResponse;
import com.vois.simpleewalletsystem.enums.TransactionStatus;
import com.vois.simpleewalletsystem.enums.TransactionType;
import com.vois.simpleewalletsystem.security.jwt.JwtAuthenticationFilter;
import com.vois.simpleewalletsystem.service.TransactionService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDepositSuccessfully() throws Exception {

        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(100));

        TransactionResponse response = new TransactionResponse();
        response.setId(1L);
        response.setAmount(BigDecimal.valueOf(100));
        response.setType(TransactionType.DEPOSIT);
        response.setStatus(TransactionStatus.SUCCESS);
        response.setDestinationWalletId(1L);

        when(transactionService.deposit(
                anyLong(),
                any(DepositRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/transactions/wallets/1/deposit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andDo(result -> {
                    System.out.println("STATUS: " + result.getResponse().getStatus());
                    System.out.println("BODY: " + result.getResponse().getContentAsString());
                });
    }
}