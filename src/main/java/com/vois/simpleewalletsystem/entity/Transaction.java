package com.vois.simpleewalletsystem.entity;

import com.vois.simpleewalletsystem.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    @ManyToOne
    @JoinColumn(name = "source_wallet_id")
    private Wallet sourceWallet;
    @ManyToOne
    @JoinColumn(name = "destination_wallet_id")
    private Wallet destinationWallet;
}
