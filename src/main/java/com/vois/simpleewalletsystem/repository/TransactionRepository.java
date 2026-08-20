package com.vois.simpleewalletsystem.repository;


import com.vois.simpleewalletsystem.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Custom query method to fetch a wallet's transaction history
    List<Transaction> findBySourceWalletIdOrDestinationWalletId(Long sourceWalletId, Long destinationWalletId);
}