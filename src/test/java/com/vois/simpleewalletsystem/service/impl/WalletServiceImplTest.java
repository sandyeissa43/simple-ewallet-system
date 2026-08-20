package com.vois.simpleewalletsystem.service.impl;

import com.vois.simpleewalletsystem.dto.generated.DepositRequest;
import com.vois.simpleewalletsystem.dto.generated.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.generated.WalletResponse;
import com.vois.simpleewalletsystem.entity.User;
import com.vois.simpleewalletsystem.entity.Wallet;
import com.vois.simpleewalletsystem.exception.WalletNotFoundException;
import com.vois.simpleewalletsystem.mapper.WalletMapper;
import com.vois.simpleewalletsystem.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletMapper walletMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void shouldGetWalletByIdSuccessfully() {

        Wallet wallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(100))
                .user(User.builder().id(2L).build())
                .build();

        WalletResponse expectedResponse = new WalletResponse()
                .id(1L)
                .balance(BigDecimal.valueOf(100))
                .userId(2L);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        when(walletMapper.toResponse(wallet))
                .thenReturn(expectedResponse);

        WalletResponse actualResponse =
                walletService.getWalletById(1L);

        assertEquals(
                expectedResponse.getId(),
                actualResponse.getId()
        );

        assertEquals(
                expectedResponse.getBalance(),
                actualResponse.getBalance()
        );

        verify(walletRepository).findById(1L);
        verify(walletMapper).toResponse(wallet);
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {

        when(walletRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.getWalletById(99L)
        );

        verify(walletRepository).findById(99L);
        verifyNoInteractions(walletMapper);
    }

    @Test
    void shouldGetWalletBalanceSuccessfully() {

        Wallet wallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(250))
                .build();

        WalletBalanceResponse expectedResponse =
                new WalletBalanceResponse()
                        .walletId(1L)
                        .balance(BigDecimal.valueOf(250));

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        when(walletMapper.toBalanceResponse(wallet))
                .thenReturn(expectedResponse);

        WalletBalanceResponse actualResponse =
                walletService.getWalletBalance(1L);

        assertEquals(
                expectedResponse.getBalance(),
                actualResponse.getBalance()
        );

        verify(walletRepository).findById(1L);
        verify(walletMapper).toBalanceResponse(wallet);
    }

    @Test
    void shouldDepositSuccessfully() {

        Wallet wallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(100))
                .user(User.builder().id(2L).build())
                .build();

        DepositRequest request = new DepositRequest()
                .amount(BigDecimal.valueOf(50));

        WalletResponse expectedResponse =
                new WalletResponse()
                        .id(1L)
                        .balance(BigDecimal.valueOf(150))
                        .userId(2L);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(any(Wallet.class)))
                .thenReturn(wallet);

        when(walletMapper.toResponse(wallet))
                .thenReturn(expectedResponse);

        WalletResponse actualResponse =
                walletService.deposit(1L, request);

        assertEquals(
                BigDecimal.valueOf(150),
                actualResponse.getBalance()
        );

        verify(walletRepository).findById(1L);
        verify(walletRepository).save(wallet);
        verify(walletMapper).toResponse(wallet);
    }

    @Test
    void shouldThrowExceptionWhenDepositingToNonExistingWallet() {

        DepositRequest request = new DepositRequest()
                .amount(BigDecimal.valueOf(50));

        when(walletRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> walletService.deposit(99L, request)
        );

        verify(walletRepository).findById(99L);
        verify(walletRepository, never())
                .save(any(Wallet.class));

        verifyNoInteractions(walletMapper);
    }
}