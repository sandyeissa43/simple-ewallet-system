package com.vois.simpleewalletsystem.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {
    private BigDecimal amount;
    private Long destinationWalletId;
}