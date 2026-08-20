package com.vois.simpleewalletsystem.service.impl;

import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.request.TransferRequest;
import com.vois.simpleewalletsystem.dto.request.WithdrawalRequest;
import com.vois.simpleewalletsystem.dto.response.TransactionResponse;
import com.vois.simpleewalletsystem.entity.Transaction;
import com.vois.simpleewalletsystem.entity.Wallet;
import com.vois.simpleewalletsystem.enums.TransactionType;
import com.vois.simpleewalletsystem.exception.InsufficientBalanceException;
import com.vois.simpleewalletsystem.exception.TransactionNotFoundException;
import com.vois.simpleewalletsystem.exception.WalletNotFoundException;
import com.vois.simpleewalletsystem.repository.TransactionRepository;
import com.vois.simpleewalletsystem.repository.WalletRepository;
import com.vois.simpleewalletsystem.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public TransactionResponse withdraw(
            Long walletId,
            WithdrawalRequest request) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found with ID: " + walletId));

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

        Wallet destinationWallet = walletRepository.findById(
                        request.getDestinationWalletId())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Destination wallet not found with ID: "
                                        + request.getDestinationWalletId()));

        if (sourceWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for transfer.");
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
        transaction.setSourceWallet(sourceWallet);
        transaction.setDestinationWallet(destinationWallet);

        transaction = transactionRepository.save(transaction);

        return convertToDTO(transaction);
    }

    @Override
    public List<TransactionResponse> getTransactionHistory(
            Long walletId) {

        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException(
                    "Wallet not found with ID: " + walletId);
        }

        List<Transaction> transactions =
                transactionRepository.findBySourceWalletIdOrDestinationWalletId(
                        walletId,
                        walletId);

        return transactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

        wallet.setBalance(
                wallet.getBalance().add(request.getAmount()));

        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setDestinationWallet(wallet);

        transaction = transactionRepository.save(transaction);

        return convertToDTO(transaction);
    }

    @Override
    public TransactionResponse getTransactionById(
            Long transactionId) {

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction not found with ID: "
                                        + transactionId));

        return convertToDTO(transaction);
    }

    private TransactionResponse convertToDTO(
            Transaction transaction) {

        TransactionResponse dto = new TransactionResponse();

        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());

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