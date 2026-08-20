package com.vois.simpleewalletsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.response.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.response.WalletResponse;
import com.vois.simpleewalletsystem.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGetWalletSuccessfully() throws Exception {
        WalletResponse response = WalletResponse.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(100))
                .userId(2L)
                .build();

        when(walletService.getWalletById(1L)).thenReturn(response);

        mockMvc.perform(get("/wallets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    void shouldGetWalletBalanceSuccessfully() throws Exception {
        WalletBalanceResponse response = WalletBalanceResponse.builder()
                .walletId(1L)
                .balance(BigDecimal.valueOf(250))
                .build();

        when(walletService.getWalletBalance(1L)).thenReturn(response);

        mockMvc.perform(get("/wallets/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(250));
    }

    @Test
    void shouldDepositSuccessfully() throws Exception {
        DepositRequest request = DepositRequest.builder()
                .amount(BigDecimal.valueOf(50))
                .build();

        WalletResponse response = WalletResponse.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(150))
                .userId(2L)
                .build();

        when(walletService.deposit(eq(1L), any(DepositRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/wallets/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150));
    }

    @Test
    void shouldReturnBadRequestWhenDepositAmountIsNegative() throws Exception {
        DepositRequest request = DepositRequest.builder()
                .amount(BigDecimal.valueOf(-50))
                .build();

        mockMvc.perform(post("/wallets/1/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}