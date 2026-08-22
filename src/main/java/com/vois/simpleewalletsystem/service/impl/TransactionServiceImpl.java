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
import org.springframework.security.core.Authentication;
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

        Wallet wallet = getAuthorizedWallet(walletId);

        checkOwnership(wallet, getCurrentUser());

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for withdrawal. Current balance: "
                            + wallet.getBalance());
        }

        wallet.setBalance(
                wallet.getBalance().subtract(request.getAmount())
        );

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


        checkOwnership(sourceWallet, getCurrentUser());

        Wallet destinationWallet = walletRepository.findById(
                        request.getDestinationWalletId())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Destination wallet not found with ID: "
                                        + request.getDestinationWalletId()));
       
        if (sourceWallet.getId().equals(destinationWallet.getId())) {
            throw new InvalidTransactionException(
                    "Source and destination wallets must be different"
            );
        }

        if (sourceWallet.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance for transfer. Current balance: "
                            + sourceWallet.getBalance()
            );
        }

        sourceWallet.setBalance(
                sourceWallet.getBalance()
                        .subtract(request.getAmount())
        );

        destinationWallet.setBalance(
                destinationWallet.getBalance()
                        .add(request.getAmount())
        );

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

        getAuthorizedWallet(walletId);

        List<Transaction> transactions =
                transactionRepository
                        .findBySourceWalletIdOrDestinationWalletIdOrderByCreatedAtDesc(
                                walletId,

                                walletId
                        );
        return transactions.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional
    public TransactionResponse deposit(
            Long walletId,
            DepositRequest request) {

        /*
         * User can only deposit into their own wallet.
         */
        Wallet wallet = getAuthorizedWallet(walletId);

        checkOwnership(wallet, getCurrentUser());

        wallet.setBalance(
                wallet.getBalance().add(request.getAmount())
        );

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
                                                + transactionId
                                )
                        );

        /*
         * Check that the authenticated user owns
         * one of the wallets involved in the transaction.
         */
        if (transaction.getSourceWallet() != null) {

            getAuthorizedWallet(
                    transaction.getSourceWallet().getId()
            );

        } else if (transaction.getDestinationWallet() != null) {

            getAuthorizedWallet(
                    transaction.getDestinationWallet().getId()
            );

        } else {

            throw new AccessDeniedException(
                    "You are not authorized to access this transaction"
            );
        }

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


    private Wallet getAuthorizedWallet(Long walletId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "User is not authenticated"
            );
        }

        String email = authentication.getName();

        User currentUser =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "Authenticated user not found"
                                )
                        );

        Wallet wallet =
                walletRepository.findById(walletId)
                        .orElseThrow(() ->
                                new WalletNotFoundException(
                                        "Wallet not found with ID: "
                                                + walletId
                                )
                        );


        if (currentUser.getRole() == Role.ADMIN) {
            return wallet;
        }


        if (wallet.getUser() == null ||
                !wallet.getUser()
                        .getId()
                        .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    "You are not authorized to access this wallet"
            );
        }

        return wallet;
    }

    private TransactionResponse convertToDTO(
            Transaction transaction) {

        TransactionResponse dto =
                new TransactionResponse();

        dto.setId(transaction.getId());

        dto.setAmount(
                transaction.getAmount()
        );

        dto.setType(
                transaction.getType()
        );

        dto.setStatus(
                transaction.getStatus()
        );

        dto.setCreatedAt(
                transaction.getCreatedAt()
        );

        if (transaction.getSourceWallet() != null) {

            dto.setSourceWalletId(
                    transaction
                            .getSourceWallet()
                            .getId()
            );
        }

        if (transaction.getDestinationWallet() != null) {

            dto.setDestinationWalletId(
                    transaction
                            .getDestinationWallet()
                            .getId()
            );
        }

        return dto;
    }
}