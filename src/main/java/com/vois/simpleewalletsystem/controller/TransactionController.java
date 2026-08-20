package com.vois.simpleewalletsystem.controller;

import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.request.TransferRequest;
import com.vois.simpleewalletsystem.dto.request.WithdrawalRequest;
import com.vois.simpleewalletsystem.dto.response.TransactionResponse;
import com.vois.simpleewalletsystem.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/wallets/{walletId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable Long walletId,
            @Valid @RequestBody WithdrawalRequest request) {

        return ResponseEntity.ok(
                transactionService.withdraw(walletId, request)
        );
    }

    @PostMapping("/wallets/{walletId}/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @PathVariable Long walletId,
            @Valid @RequestBody TransferRequest request) {

        return ResponseEntity.ok(
                transactionService.transfer(walletId, request)
        );
    }

    @GetMapping("/wallets/{walletId}/history")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable Long walletId) {

        return ResponseEntity.ok(
                transactionService.getTransactionHistory(walletId)
        );
    }

    @PostMapping("/wallets/{walletId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable Long walletId,
            @Valid @RequestBody DepositRequest request) {

        return ResponseEntity.ok(
                transactionService.deposit(walletId, request)
        );
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable Long transactionId) {

        return ResponseEntity.ok(
                transactionService.getTransactionById(transactionId)
        );
    }
}