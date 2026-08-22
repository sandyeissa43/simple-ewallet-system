package com.vois.simpleewalletsystem.controller;

import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.response.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.response.WalletResponse;
import com.vois.simpleewalletsystem.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.model.DepositRequest;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/{walletId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable Long walletId,
            Authentication authentication) {

        return ResponseEntity.ok(
                walletService.getWalletById(
                        walletId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{walletId}/balance")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<WalletBalanceResponse> getBalance(
            @PathVariable Long walletId,
            Authentication authentication) {

        return ResponseEntity.ok(
                walletService.getWalletBalance(
                        walletId,
                        authentication.getName()
                )
        );
    }

    @PostMapping("/{walletId}/deposit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<WalletResponse> deposit(
            @PathVariable Long walletId,
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                walletService.deposit(
                        walletId,
                        request,
                        authentication.getName()
                )
        );
    }
}