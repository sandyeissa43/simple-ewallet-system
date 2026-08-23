package com.vois.simpleewalletsystem.service;

import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.request.TransferRequest;
import com.vois.simpleewalletsystem.dto.request.WithdrawalRequest;
import com.vois.simpleewalletsystem.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse withdraw(
            Long walletId,
            WithdrawalRequest request
    );
    TransactionResponse transfer(
            Long walletId,
            TransferRequest request
    );
    TransactionResponse deposit(
            Long walletId,
            DepositRequest request
    );
    List<TransactionResponse> getTransactionHistory(
            Long walletId
    );
    TransactionResponse getTransactionById(
            Long transactionId
    );
}