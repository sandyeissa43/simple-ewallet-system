package com.vois.simpleewalletsystem.service.impl;

import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.request.TransferRequest;
import com.vois.simpleewalletsystem.dto.request.WithdrawalRequest;
import com.vois.simpleewalletsystem.dto.response.TransactionResponse;
import com.vois.simpleewalletsystem.entity.Transaction;
import com.vois.simpleewalletsystem.entity.User;
import com.vois.simpleewalletsystem.entity.Wallet;
import com.vois.simpleewalletsystem.enums.Role;
import com.vois.simpleewalletsystem.enums.TransactionStatus;
import com.vois.simpleewalletsystem.enums.TransactionType;
import com.vois.simpleewalletsystem.exception.InsufficientBalanceException;
import com.vois.simpleewalletsystem.exception.InvalidTransactionException;
import com.vois.simpleewalletsystem.exception.TransactionNotFoundException;
import com.vois.simpleewalletsystem.exception.WalletNotFoundException;
import com.vois.simpleewalletsystem.repository.TransactionRepository;
import com.vois.simpleewalletsystem.repository.UserRepository;
import com.vois.simpleewalletsystem.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User currentUser;

    @BeforeEach
    void setUp() {

        currentUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.USER)
                .build();

        SecurityContextHolder.setContext(securityContext);

        lenient().when(securityContext.getAuthentication())
                .thenReturn(authentication);

        lenient().when(authentication.isAuthenticated())
                .thenReturn(true);

        lenient().when(authentication.getName())
                .thenReturn("test@example.com");

        lenient().when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(currentUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Wallet createWallet(
            Long walletId,
            BigDecimal balance) {

        return Wallet.builder()
                .id(walletId)
                .balance(balance)
                .user(currentUser)
                .build();
    }


    @Test
    void shouldDepositSuccessfully() {

        Wallet wallet = createWallet(
                1L,
                BigDecimal.valueOf(100)
        );

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

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                BigDecimal.valueOf(50),
                response.getAmount()
        );

        assertEquals(
                TransactionType.DEPOSIT,
                response.getType()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                response.getStatus()
        );

        assertEquals(
                1L,
                response.getDestinationWalletId()
        );

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

        verify(walletRepository, never())
                .save(any());

        verify(transactionRepository, never())
                .save(any());
    }


    @Test
    void shouldWithdrawSuccessfully() {

        Wallet wallet = createWallet(
                1L,
                BigDecimal.valueOf(200)
        );

        WithdrawalRequest request =
                new WithdrawalRequest();

        request.setAmount(
                BigDecimal.valueOf(50)
        );

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

        assertEquals(
                2L,
                response.getId()
        );

        assertEquals(
                BigDecimal.valueOf(50),
                response.getAmount()
        );

        assertEquals(
                TransactionType.WITHDRAW,
                response.getType()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                response.getStatus()
        );

        assertEquals(
                1L,
                response.getSourceWalletId()
        );

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

        Wallet wallet = createWallet(
                1L,
                BigDecimal.valueOf(30)
        );

        WithdrawalRequest request =
                new WithdrawalRequest();

        request.setAmount(
                BigDecimal.valueOf(50)
        );

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

        verify(walletRepository, never())
                .save(any());

        verify(transactionRepository, never())
                .save(any());
    }


    @Test
    void shouldTransferSuccessfully() {

        Wallet sourceWallet = createWallet(
                1L,
                BigDecimal.valueOf(200)
        );

        Wallet destinationWallet = createWallet(
                2L,
                BigDecimal.valueOf(100)
        );

        TransferRequest request =
                new TransferRequest();

        request.setAmount(
                BigDecimal.valueOf(50)
        );

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

        assertEquals(
                3L,
                response.getId()
        );

        assertEquals(
                BigDecimal.valueOf(50),
                response.getAmount()
        );

        assertEquals(
                TransactionType.TRANSFER,
                response.getType()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                response.getStatus()
        );

        assertEquals(
                1L,
                response.getSourceWalletId()
        );

        assertEquals(
                2L,
                response.getDestinationWalletId()
        );

        assertEquals(
                BigDecimal.valueOf(150),
                sourceWallet.getBalance()
        );

        assertEquals(
                BigDecimal.valueOf(150),
                destinationWallet.getBalance()
        );

        verify(walletRepository).findById(1L);
        verify(walletRepository).findById(2L);

        verify(walletRepository)
                .save(sourceWallet);

        verify(walletRepository)
                .save(destinationWallet);

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenSourceWalletNotFoundForTransfer() {

        TransferRequest request =
                new TransferRequest();

        request.setAmount(
                BigDecimal.valueOf(50)
        );

        request.setDestinationWalletId(2L);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> transactionService.transfer(1L, request)
        );

        verify(walletRepository)
                .findById(1L);

        verify(walletRepository, never())
                .save(any());

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficientForTransfer() {

        Wallet sourceWallet = createWallet(
                1L,
                BigDecimal.valueOf(30)
        );

        Wallet destinationWallet = createWallet(
                2L,
                BigDecimal.valueOf(100)
        );

        TransferRequest request =
                new TransferRequest();

        request.setAmount(
                BigDecimal.valueOf(50)
        );

        request.setDestinationWalletId(2L);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(sourceWallet));

        when(walletRepository.findById(2L))
                .thenReturn(Optional.of(destinationWallet));

        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.transfer(1L, request)
        );

        assertEquals(
                BigDecimal.valueOf(30),
                sourceWallet.getBalance()
        );

        assertEquals(
                BigDecimal.valueOf(100),
                destinationWallet.getBalance()
        );

        verify(walletRepository)
                .findById(1L);

        verify(walletRepository)
                .findById(2L);

        verify(walletRepository, never())
                .save(any());

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenTransferToSameWallet() {

        Wallet wallet = createWallet(
                1L,
                BigDecimal.valueOf(200)
        );

        TransferRequest request =
                new TransferRequest();

        request.setAmount(
                BigDecimal.valueOf(50)
        );

        request.setDestinationWalletId(1L);

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                InvalidTransactionException.class,
                () -> transactionService.transfer(1L, request)
        );

        assertEquals(
                BigDecimal.valueOf(200),
                wallet.getBalance()
        );

        verify(walletRepository, times(2))
                .findById(1L);

        verify(walletRepository, never())
                .save(any());

        verify(transactionRepository, never())
                .save(any());
    }


    @Test
    void shouldGetTransactionByIdSuccessfully() {

        Wallet wallet = createWallet(
                1L,
                BigDecimal.valueOf(100)
        );

        Transaction transaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .destinationWallet(wallet)
                .build();

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        TransactionResponse response =
                transactionService.getTransactionById(1L);

        assertNotNull(response);

        assertEquals(
                1L,
                response.getId()
        );

        assertEquals(
                BigDecimal.valueOf(100),
                response.getAmount()
        );

        assertEquals(
                TransactionType.DEPOSIT,
                response.getType()
        );

        assertEquals(
                TransactionStatus.SUCCESS,
                response.getStatus()
        );

        assertEquals(
                1L,
                response.getDestinationWalletId()
        );

        verify(transactionRepository)
                .findById(1L);

        verify(walletRepository)
                .findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTransactionIsNotFound() {

        when(transactionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(999L)
        );

        verify(transactionRepository)
                .findById(999L);

        verify(walletRepository, never())
                .findById(anyLong());
    }



    @Test
    void shouldGetTransactionHistorySuccessfully() {

        Wallet wallet = createWallet(
                1L,
                BigDecimal.valueOf(200)
        );

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

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        when(transactionRepository
                .findBySourceWalletIdOrDestinationWalletIdOrderByCreatedAtDesc(
                        1L,
                        1L
                ))
                .thenReturn(
                        List.of(transaction1, transaction2)
                );

        List<TransactionResponse> responses =
                transactionService.getTransactionHistory(1L);

        assertNotNull(responses);

        assertEquals(
                2,
                responses.size()
        );

        assertEquals(
                1L,
                responses.get(0).getId()
        );

        assertEquals(
                BigDecimal.valueOf(50),
                responses.get(0).getAmount()
        );

        assertEquals(
                TransactionType.DEPOSIT,
                responses.get(0).getType()
        );

        assertEquals(
                2L,
                responses.get(1).getId()
        );

        assertEquals(
                BigDecimal.valueOf(30),
                responses.get(1).getAmount()
        );

        assertEquals(
                TransactionType.WITHDRAW,
                responses.get(1).getType()
        );

        verify(walletRepository)
                .findById(1L);

        verify(transactionRepository)
                .findBySourceWalletIdOrDestinationWalletIdOrderByCreatedAtDesc(
                        1L,
                        1L
                );
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFoundForTransactionHistory() {

        when(walletRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                WalletNotFoundException.class,
                () -> transactionService.getTransactionHistory(999L)
        );

        verify(walletRepository)
                .findById(999L);

        verify(transactionRepository, never())
                .findBySourceWalletIdOrDestinationWalletIdOrderByCreatedAtDesc(
                        anyLong(),
                        anyLong()
                );
    }


    @Test
    void shouldThrowExceptionWhenUserIsNotAuthenticated() {

        when(securityContext.getAuthentication())
                .thenReturn(null);

        Wallet wallet = createWallet(
                1L,
                BigDecimal.valueOf(100)
        );

        DepositRequest request =
                new DepositRequest();

        request.setAmount(
                BigDecimal.valueOf(50)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> transactionService.deposit(1L, request)
        );

        verify(walletRepository, never())
                .findById(anyLong());

        verify(walletRepository, never())
                .save(any());

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAuthorizedForWallet() {

        User anotherUser = User.builder()
                .id(99L)
                .email("another@example.com")
                .role(Role.USER)
                .build();

        Wallet wallet = Wallet.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(100))
                .user(anotherUser)
                .build();

        DepositRequest request =
                new DepositRequest();

        request.setAmount(
                BigDecimal.valueOf(50)
        );

        when(walletRepository.findById(1L))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                AccessDeniedException.class,
                () -> transactionService.deposit(1L, request)
        );

        verify(walletRepository)
                .findById(1L);

        verify(walletRepository, never())
                .save(any());

        verify(transactionRepository, never())
                .save(any());
    }
}