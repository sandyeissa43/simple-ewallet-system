package com.vois.simpleewalletsystem.controller;

import com.vois.simpleewalletsystem.dto.generated.DepositRequest;
import com.vois.simpleewalletsystem.dto.generated.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.generated.WalletResponse;
import com.vois.simpleewalletsystem.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable Long walletId) {

        return ResponseEntity.ok(
                walletService.getWalletById(walletId)
        );
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(
            @PathVariable Long walletId) {

        return ResponseEntity.ok(
                walletService.getWalletBalance(walletId)
        );
    }

    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<WalletResponse> deposit(
            @PathVariable Long walletId,
            @Valid @RequestBody DepositRequest request) {

        return ResponseEntity.ok(
                walletService.deposit(walletId, request)
        );
    }
}