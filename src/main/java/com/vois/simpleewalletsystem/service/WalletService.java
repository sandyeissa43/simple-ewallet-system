package com.vois.simpleewalletsystem.service;

import com.vois.simpleewalletsystem.dto.response.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.response.WalletResponse;
import com.vois.simpleewalletsystem.entity.User;

public interface WalletService {
    WalletResponse createWallet(User user);
    WalletResponse getWalletById(Long walletId);
    WalletBalanceResponse getWalletBalance(Long walletId);
}