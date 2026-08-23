package com.vois.simpleewalletsystem.service;

import com.vois.simpleewalletsystem.dto.generated.DepositRequest;
import com.vois.simpleewalletsystem.dto.generated.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.generated.WalletResponse;
import com.vois.simpleewalletsystem.entity.User;

import jakarta.validation.Valid;


public interface WalletService {

    WalletResponse createWallet(User user);
    WalletResponse getWalletById(Long walletId, String userEmail);
    WalletBalanceResponse getWalletBalance(Long walletId, String userEmail);
    WalletResponse deposit(
            Long walletId,
            @Valid DepositRequest request,
            String userEmail
    );
}