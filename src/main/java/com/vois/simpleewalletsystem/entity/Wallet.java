package com.vois.simpleewalletsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Builder.Default
    private  BigDecimal balance = BigDecimal.ZERO;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private  User user;
    @OneToMany(mappedBy = "wallet",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

}
