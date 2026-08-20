package com.vois.simpleewalletsystem.service.impl;

import com.vois.simpleewalletsystem.dto.generated.DepositRequest;
import com.vois.simpleewalletsystem.dto.generated.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.generated.WalletResponse;
import com.vois.simpleewalletsystem.entity.User;
import com.vois.simpleewalletsystem.entity.Wallet;
import com.vois.simpleewalletsystem.exception.WalletNotFoundException;
import com.vois.simpleewalletsystem.mapper.WalletMapper;
import com.vois.simpleewalletsystem.repository.WalletRepository;
import com.vois.simpleewalletsystem.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    public WalletResponse createWallet(User user) {

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        return walletMapper.toResponse(savedWallet);
    }

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
    public WalletResponse deposit(
            Long walletId,
            DepositRequest request) {

        Wallet wallet = findWalletOrThrow(walletId);

        wallet.setBalance(
                wallet.getBalance().add(request.getAmount())
        );

        Wallet savedWallet = walletRepository.save(wallet);

        return walletMapper.toResponse(savedWallet);
    }

    private Wallet findWalletOrThrow(Long walletId) {

        return walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found with id: " + walletId
                        )
                );
    }
}
