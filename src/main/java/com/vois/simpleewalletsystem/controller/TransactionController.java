package com.vois.simpleewalletsystem.controller;

import com.example.api.TransactionsApi;
import com.example.model.TransactionStatus;
import com.example.model.TransactionType;
import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.request.TransferRequest;
import com.vois.simpleewalletsystem.dto.request.WithdrawalRequest;
import com.vois.simpleewalletsystem.dto.response.TransactionResponse;
import com.vois.simpleewalletsystem.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransactionController implements TransactionsApi {

    private final TransactionService transactionService;

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<com.example.model.TransactionResponse> deposit(
            Long walletId,
            com.example.model.DepositRequest request) {

        DepositRequest serviceRequest = new DepositRequest();

        serviceRequest.setAmount(
                BigDecimal.valueOf(request.getAmount())
        );

        TransactionResponse response =
                transactionService.deposit(walletId, serviceRequest);

        return ResponseEntity.ok(toGeneratedResponse(response));
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<com.example.model.TransactionResponse> withdraw(
            Long walletId,
            com.example.model.WithdrawalRequest request) {

        WithdrawalRequest serviceRequest = new WithdrawalRequest();

        serviceRequest.setAmount(
                BigDecimal.valueOf(request.getAmount())
        );

        TransactionResponse response =
                transactionService.withdraw(walletId, serviceRequest);

        return ResponseEntity.ok(toGeneratedResponse(response));
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<com.example.model.TransactionResponse> transfer(
            Long walletId,
            com.example.model.TransferRequest request) {

        TransferRequest serviceRequest = new TransferRequest();

        serviceRequest.setAmount(
                BigDecimal.valueOf(request.getAmount())
        );

        serviceRequest.setDestinationWalletId(
                request.getDestinationWalletId()
        );

        TransactionResponse response =
                transactionService.transfer(walletId, serviceRequest);

        return ResponseEntity.ok(toGeneratedResponse(response));
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<com.example.model.TransactionResponse>>
    getTransactionHistory(Long walletId) {

        List<TransactionResponse> responses =
                transactionService.getTransactionHistory(walletId);

        List<com.example.model.TransactionResponse> generatedResponses =
                responses.stream()
                        .map(this::toGeneratedResponse)
                        .toList();

        return ResponseEntity.ok(generatedResponses);
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<com.example.model.TransactionResponse>
    getTransactionById(Long transactionId) {

        TransactionResponse response =
                transactionService.getTransactionById(transactionId);

        return ResponseEntity.ok(toGeneratedResponse(response));
    }

    private com.example.model.TransactionResponse toGeneratedResponse(
            TransactionResponse response) {

        com.example.model.TransactionResponse generated =
                new com.example.model.TransactionResponse();

        generated.setId(response.getId());

        generated.setAmount(
                response.getAmount() != null
                        ? response.getAmount().doubleValue()
                        : null
        );

        generated.setType(
                response.getType() != null
                        ? TransactionType.valueOf(response.getType().name())
                        : null
        );

        generated.setStatus(
                response.getStatus() != null
                        ? TransactionStatus.valueOf(response.getStatus().name())
                        : null
        );

        generated.setCreatedAt(
                response.getCreatedAt() != null
                        ? response.getCreatedAt().atOffset(
                        java.time.ZoneOffset.UTC)
                        : null
        );

        generated.setSourceWalletId(
                response.getSourceWalletId() != null
                        ? JsonNullable.of(response.getSourceWalletId())
                        : JsonNullable.undefined()
        );

        generated.setDestinationWalletId(
                response.getDestinationWalletId() != null
                        ? JsonNullable.of(response.getDestinationWalletId())
                        : JsonNullable.undefined()
        );

        return generated;
    }
}