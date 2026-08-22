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
import com.vois.simpleewalletsystem.exception.UserNotFoundException;
import com.vois.simpleewalletsystem.exception.WalletNotFoundException;
import com.vois.simpleewalletsystem.repository.TransactionRepository;
import com.vois.simpleewalletsystem.repository.UserRepository;
import com.vois.simpleewalletsystem.repository.WalletRepository;
import com.vois.simpleewalletsystem.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransactionResponse withdraw(
            Long walletId,
            WithdrawalRequest request) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found with ID: " + walletId));

        checkOwnership(wallet, getCurrentUser());

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for withdrawal. Current balance: "
                            + wallet.getBalance());
        }

        wallet.setBalance(
                wallet.getBalance().subtract(request.getAmount()));

        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setSourceWallet(wallet);

        transaction = transactionRepository.save(transaction);

        return convertToDTO(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse transfer(
            Long walletId,
            TransferRequest request) {

        Wallet sourceWallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Source wallet not found with ID: "
                                        + walletId));

        checkOwnership(sourceWallet, getCurrentUser());

        Wallet destinationWallet = walletRepository.findById(
                        request.getDestinationWalletId())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Destination wallet not found with ID: "
                                        + request.getDestinationWalletId()));

        if (sourceWallet.getId().equals(destinationWallet.getId())) {
            throw new InvalidTransactionException(
                    "Source and destination wallets must be different");
        }

        if (sourceWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for transfer. Current balance: "
                            + sourceWallet.getBalance());
        }

        sourceWallet.setBalance(
                sourceWallet.getBalance().subtract(request.getAmount()));

        destinationWallet.setBalance(
                destinationWallet.getBalance().add(request.getAmount()));

        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setSourceWallet(sourceWallet);
        transaction.setDestinationWallet(destinationWallet);

        transaction = transactionRepository.save(transaction);

        return convertToDTO(transaction);
    }

    @Override
    public List<TransactionResponse> getTransactionHistory(
            Long walletId) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found with ID: " + walletId));

        checkOwnership(wallet, getCurrentUser());

        List<Transaction> transactions =
                transactionRepository
                        .findBySourceWalletIdOrDestinationWalletIdOrderByCreatedAtDesc(
                                walletId,
                                walletId);

        return transactions.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional
    public TransactionResponse deposit(
            Long walletId,
            DepositRequest request) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found with ID: " + walletId));

        checkOwnership(wallet, getCurrentUser());

        wallet.setBalance(
                wallet.getBalance().add(request.getAmount()));

        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDestinationWallet(wallet);

        transaction = transactionRepository.save(transaction);

        return convertToDTO(transaction);
    }

    @Override
    public TransactionResponse getTransactionById(
            Long transactionId) {

        Transaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new TransactionNotFoundException(
                                        "Transaction not found with ID: "
                                                + transactionId));

        User currentUser = getCurrentUser();

        boolean isSourceOwner = transaction.getSourceWallet() != null
                && transaction.getSourceWallet().getUser().getId()
                .equals(currentUser.getId());

        boolean isDestinationOwner = transaction.getDestinationWallet() != null
                && transaction.getDestinationWallet().getUser().getId()
                .equals(currentUser.getId());

        if (currentUser.getRole() != Role.ADMIN
                && !isSourceOwner
                && !isDestinationOwner) {

            throw new AccessDeniedException(
                    "You do not have access to this transaction"
            );
        }

        return convertToDTO(transaction);
    }

    private User getCurrentUser() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
    }

    private void checkOwnership(Wallet wallet, User user) {

        if (user.getRole() == Role.ADMIN) {
            return;
        }

        if (!wallet.getUser().getId().equals(user.getId())) {

            throw new AccessDeniedException(
                    "You do not have access to this wallet"
            );
        }
    }

    private TransactionResponse convertToDTO(
            Transaction transaction) {

        TransactionResponse dto = new TransactionResponse();

        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setStatus(transaction.getStatus());
        dto.setCreatedAt(transaction.getCreatedAt());

        if (transaction.getSourceWallet() != null) {
            dto.setSourceWalletId(
                    transaction.getSourceWallet().getId());
        }

        if (transaction.getDestinationWallet() != null) {
            dto.setDestinationWalletId(
                    transaction.getDestinationWallet().getId());
        }

        return dto;
    }
}