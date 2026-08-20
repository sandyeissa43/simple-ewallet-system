package com.vois.simpleewalletsystem.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WithdrawalRequest {

    private BigDecimal amount;

}
