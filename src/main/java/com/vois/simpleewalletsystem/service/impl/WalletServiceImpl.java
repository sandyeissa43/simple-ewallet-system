package com.vois.simpleewalletsystem.service.impl;

import com.vois.simpleewalletsystem.dto.request.DepositRequest;
import com.vois.simpleewalletsystem.dto.response.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.response.WalletResponse;
import com.vois.simpleewalletsystem.entity.Wallet;
import com.vois.simpleewalletsystem.exception.WalletNotFoundException;
import com.vois.simpleewalletsystem.mapper.WalletMapper;
import com.vois.simpleewalletsystem.repository.WalletRepository;
import com.vois.simpleewalletsystem.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletMapper walletMapper;

    @Override
    public WalletResponse getWalletById(Long walletId) {
        Wallet wallet = findWalletOrThrow(walletId);
        return walletMapper.toResponse(wallet);
    }

    @Override
    public WalletBalanceResponse getWalletBalance(Long walletId) {
        Wallet wallet = findWalletOrThrow(walletId);
        return walletMapper.toBalanceResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse deposit(Long walletId, DepositRequest request) {
        Wallet wallet = findWalletOrThrow(walletId);
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toResponse(savedWallet);
    }

    private Wallet findWalletOrThrow(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));
    }
}
