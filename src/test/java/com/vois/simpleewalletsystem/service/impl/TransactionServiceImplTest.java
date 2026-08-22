package com.vois.simpleewalletsystem.service.impl;

import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.response.TransactionResponse;
import com.vois.simpleewalletsystem.entity.Transaction;
import com.vois.simpleewalletsystem.entity.Wallet;
import com.vois.simpleewalletsystem.enums.TransactionStatus;
import com.vois.simpleewalletsystem.enums.TransactionType;
import com.vois.simpleewalletsystem.repository.TransactionRepository;
import com.vois.simpleewalletsystem.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vois.simpleewalletsystem.dto.request.WithdrawalRequest;
import com.vois.simpleewalletsystem.exception.InsufficientBalanceException;
import com.vois.simpleewalletsystem.exception.InvalidTransactionException;
import com.vois.simpleewalletsystem.exception.TransactionNotFoundException;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;
import com.vois.simpleewalletsystem.dto.request.TransferRequest;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.vois.simpleewalletsystem.exception.WalletNotFoundException;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void shouldDepositSuccessfully() {

        Wallet wallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(100))
                .build();

        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(50));

        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(50))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .destinationWallet(wallet)
                .build();

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);


        TransactionResponse response =
                transactionService.deposit(1L, request);


        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(BigDecimal.valueOf(50), response.getAmount());
        assertEquals(TransactionType.DEPOSIT, response.getType());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(1L, response.getDestinationWalletId());

        assertEquals(
                BigDecimal.valueOf(150),
                wallet.getBalance()
        );

        verify(walletRepository).findById(1L);
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }
    @Test
    void shouldThrowExceptionWhenWalletNotFoundForDeposit() {

        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(50));

        when(walletRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> transactionService.deposit(1L, request)
        );

        verify(walletRepository).findById(1L);
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
    @Test
    void shouldWithdrawSuccessfully() {

        Wallet wallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(200))
                .build();

        WithdrawalRequest request = new WithdrawalRequest();
        request.setAmount(BigDecimal.valueOf(50));

        Transaction transaction = Transaction.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(50))
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.SUCCESS)
                .sourceWallet(wallet)
                .build();

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        TransactionResponse response =
                transactionService.withdraw(1L, request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(BigDecimal.valueOf(50), response.getAmount());
        assertEquals(TransactionType.WITHDRAW, response.getType());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(1L, response.getSourceWalletId());

        assertEquals(
                BigDecimal.valueOf(150),
                wallet.getBalance()
        );

        verify(walletRepository).findById(1L);
        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(any(Transaction.class));
    }
    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficientForWithdrawal() {

        Wallet wallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(30))
                .build();

        WithdrawalRequest request = new WithdrawalRequest();
        request.setAmount(BigDecimal.valueOf(50));

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.withdraw(1L, request)
        );

        assertEquals(
                BigDecimal.valueOf(30),
                wallet.getBalance()
        );

        verify(walletRepository).findById(1L);
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
    @Test
    void shouldTransferSuccessfully() {

        Wallet sourceWallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(200))
                .build();

        Wallet destinationWallet = Wallet.builder()
                .id(2L)
                .balance(BigDecimal.valueOf(100))
                .build();

        TransferRequest request = new TransferRequest();
        request.setAmount(BigDecimal.valueOf(50));
        request.setDestinationWalletId(2L);

        Transaction transaction = Transaction.builder()
                .id(3L)
                .amount(BigDecimal.valueOf(50))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .sourceWallet(sourceWallet)
                .destinationWallet(destinationWallet)
                .build();

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(sourceWallet));

        when(walletRepository.findById(2L))
                .thenReturn(Optional.of(destinationWallet));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        TransactionResponse response =
                transactionService.transfer(1L, request);

        assertNotNull(response);

        assertEquals(3L, response.getId());
        assertEquals(BigDecimal.valueOf(50), response.getAmount());
        assertEquals(TransactionType.TRANSFER, response.getType());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());

        assertEquals(1L, response.getSourceWalletId());
        assertEquals(2L, response.getDestinationWalletId());

        // Check balances
        assertEquals(
                BigDecimal.valueOf(150),
                sourceWallet.getBalance()
        );

        assertEquals(
                BigDecimal.valueOf(150),
                destinationWallet.getBalance()
        );

        // Verify repository calls
        verify(walletRepository).findById(1L);
        verify(walletRepository).findById(2L);

        verify(walletRepository).save(sourceWallet);
        verify(walletRepository).save(destinationWallet);

        verify(transactionRepository).save(any(Transaction.class));
    }
    @Test
    void shouldThrowExceptionWhenSourceWalletNotFoundForTransfer() {

        TransferRequest request = new TransferRequest();
        request.setAmount(BigDecimal.valueOf(50));
        request.setDestinationWalletId(2L);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> transactionService.transfer(1L, request)
        );

        verify(walletRepository).findById(1L);

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficientForTransfer() {

        Wallet sourceWallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(30))
                .build();

        Wallet destinationWallet = Wallet.builder()
                .id(2L)
                .balance(BigDecimal.valueOf(100))
                .build();

        TransferRequest request = new TransferRequest();
        request.setAmount(BigDecimal.valueOf(50));
        request.setDestinationWalletId(2L);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(sourceWallet));

        when(walletRepository.findById(2L))
                .thenReturn(Optional.of(destinationWallet));

        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.transfer(1L, request)
        );

        // Balances should remain unchanged
        assertEquals(
                BigDecimal.valueOf(30),
                sourceWallet.getBalance()
        );

        assertEquals(
                BigDecimal.valueOf(100),
                destinationWallet.getBalance()
        );

        verify(walletRepository).findById(1L);
        verify(walletRepository).findById(2L);

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
    @Test
    void shouldThrowExceptionWhenTransferToSameWallet() {

        Wallet wallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(200))
                .build();

        TransferRequest request = new TransferRequest();
        request.setAmount(BigDecimal.valueOf(50));
        request.setDestinationWalletId(1L);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                InvalidTransactionException.class,
                () -> transactionService.transfer(1L, request)
        );

        // Balance must not change
        assertEquals(
                BigDecimal.valueOf(200),
                wallet.getBalance()
        );

        verify(walletRepository, times(2)).findById(1L);


        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
    @Test
    void shouldGetTransactionByIdSuccessfully() {

        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .build();

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        TransactionResponse response =
                transactionService.getTransactionById(1L);

        assertEquals(1L, response.getId());
        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        assertEquals(TransactionType.DEPOSIT, response.getType());
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());

        verify(transactionRepository).findById(1L);
    }
    @Test
    void shouldThrowExceptionWhenTransactionIsNotFound() {

        when(transactionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(999L)
        );

        verify(transactionRepository).findById(999L);
    }
    @Test
    void shouldGetTransactionHistorySuccessfully() {

        Wallet wallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(200))
                .build();

        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(50))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .destinationWallet(wallet)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(30))
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.SUCCESS)
                .sourceWallet(wallet)
                .build();

        when(walletRepository.existsById(1L))
                .thenReturn(true);

        when(transactionRepository
                .findBySourceWalletIdOrDestinationWalletIdOrderByCreatedAtDesc(1L, 1L))
                .thenReturn(List.of(transaction1, transaction2));

        List<TransactionResponse> responses =
                transactionService.getTransactionHistory(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).getId());
        assertEquals(BigDecimal.valueOf(50), responses.get(0).getAmount());
        assertEquals(
                TransactionType.DEPOSIT,
                responses.get(0).getType()
        );

        assertEquals(2L, responses.get(1).getId());
        assertEquals(BigDecimal.valueOf(30), responses.get(1).getAmount());
        assertEquals(
                TransactionType.WITHDRAW,
                responses.get(1).getType()
        );

        verify(walletRepository).existsById(1L);

        verify(transactionRepository)
                .findBySourceWalletIdOrDestinationWalletIdOrderByCreatedAtDesc(
                        1L,
                        1L
                );
    }
    @Test
    void shouldThrowExceptionWhenWalletNotFoundForTransactionHistory() {

        when(walletRepository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                WalletNotFoundException.class,
                () -> transactionService.getTransactionHistory(999L)
        );

        verify(walletRepository).existsById(999L);

        verify(transactionRepository, never())
                .findBySourceWalletIdOrDestinationWalletIdOrderByCreatedAtDesc(
                        anyLong(),
                        anyLong()
                );
    }
}



