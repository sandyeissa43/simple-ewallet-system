package com.vois.simpleewalletsystem.controller;

import com.vois.simpleewalletsystem.dto.response.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.response.WalletResponse;
import com.vois.simpleewalletsystem.security.jwt.JwtAuthenticationFilter;
import com.vois.simpleewalletsystem.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldGetWalletSuccessfully() throws Exception {

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("test@example.com");

        WalletResponse response = mock(WalletResponse.class);

        when(response.getId())
                .thenReturn(1L);

        when(response.getBalance())
                .thenReturn(java.math.BigDecimal.valueOf(100));

        when(walletService.getWalletById(
                1L,
                "test@example.com"
        )).thenReturn(response);

        mockMvc.perform(
                        get("/wallets/1")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    void shouldGetWalletBalanceSuccessfully() throws Exception {

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("test@example.com");

        WalletBalanceResponse response = mock(WalletBalanceResponse.class);

        when(response.getWalletId())
                .thenReturn(1L);

        when(response.getBalance())
                .thenReturn(java.math.BigDecimal.valueOf(250));

        when(walletService.getWalletBalance(
                1L,
                "test@example.com"
        )).thenReturn(response);

        mockMvc.perform(
                        get("/wallets/1/balance")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(250));
    }
}