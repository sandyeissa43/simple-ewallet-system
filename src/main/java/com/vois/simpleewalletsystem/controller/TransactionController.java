package com.vois.simpleewalletsystem.controller;

import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.request.TransferRequest;
import com.vois.simpleewalletsystem.dto.request.WithdrawalRequest;
import com.vois.simpleewalletsystem.dto.response.TransactionResponse;
import com.vois.simpleewalletsystem.service.impl.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionServiceImpl transactionService;

    // 1. Withdrawal Endpoint
    @PostMapping("/wallets/{walletId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable Long walletId,
            @RequestBody WithdrawalRequest request) {

        TransactionResponse response =
                transactionService.withdraw(walletId, request);

        return ResponseEntity.ok(response);
    }

    // 2. Transfer Endpoint
    @PostMapping("/wallets/{walletId}/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @PathVariable Long walletId,
            @RequestBody TransferRequest request) {

        TransactionResponse response =
                transactionService.transfer(walletId, request);

        return ResponseEntity.ok(response);
    }

    // 3. Transaction History Endpoint
    @GetMapping("/wallets/{walletId}/history")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable Long walletId) {

        List<TransactionResponse> response =
                transactionService.getTransactionHistory(walletId);

        return ResponseEntity.ok(response);
    }

    // 4. Deposit Endpoint
    @PostMapping("/wallets/{walletId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable Long walletId,
            @RequestBody DepositRequest request) {

        TransactionResponse response =
                transactionService.deposit(walletId, request);

        return ResponseEntity.ok(response);
    }

    // 5. Get Transaction By ID Endpoint
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long transactionId) {

        TransactionResponse response =
                transactionService.getTransactionById(transactionId);

        return ResponseEntity.ok(response);
    }
}