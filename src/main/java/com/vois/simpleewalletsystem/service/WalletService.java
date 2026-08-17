package com.vois.simpleewalletsystem.service;

import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.response.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.response.WalletResponse;

public interface WalletService {
    WalletResponse getWalletById(Long walletId);
    WalletBalanceResponse getWalletBalance(Long walletId);
    WalletResponse deposit(Long walletId, DepositRequest request);
}