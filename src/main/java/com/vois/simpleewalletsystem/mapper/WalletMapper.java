package com.vois.simpleewalletsystem.mapper;

import com.vois.simpleewalletsystem.dto.generated.WalletBalanceResponse;
import com.vois.simpleewalletsystem.dto.generated.WalletResponse;
import com.vois.simpleewalletsystem.entity.Wallet;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class WalletMapper {

    public WalletResponse toResponse(Wallet wallet) {

        return new WalletResponse()
                .id(wallet.getId())
                .balance(wallet.getBalance())
                .version(wallet.getVersion())
                .userId(wallet.getUser().getId())
                .createdAt(toOffsetDateTime(wallet.getCreatedAt()))
                .updatedAt(toOffsetDateTime(wallet.getUpdatedAt()));
    }

    public WalletBalanceResponse toBalanceResponse(Wallet wallet) {

        return new WalletBalanceResponse()
                .walletId(wallet.getId())
                .balance(wallet.getBalance());
    }

    private OffsetDateTime toOffsetDateTime(
            java.time.LocalDateTime dateTime) {

        if (dateTime == null) {
            return null;
        }

        return dateTime.atOffset(ZoneOffset.UTC);
    }
}