package com.vois.simpleewalletsystem.controller;

import com.vois.simpleewalletsystem.dto.response.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.response.WalletResponse;
import com.vois.simpleewalletsystem.security.jwt.JwtAuthenticationFilter;
import com.vois.simpleewalletsystem.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.context.annotation.Import;
@WebMvcTest(controllers = WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WalletController.class)

class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;


    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldGetWalletSuccessfully() throws Exception {

        WalletResponse response = WalletResponse.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(100))
                .userId(2L)
                .build();

        when(walletService.getWalletById(1L))
                .thenReturn(response);

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

        when(walletService.getWalletBalance(1L))
                .thenReturn(response);

        mockMvc.perform(get("/wallets/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(250));
    }
}