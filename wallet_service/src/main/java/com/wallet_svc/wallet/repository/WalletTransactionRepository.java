package com.wallet_svc.wallet.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wallet_svc.wallet.entity.WalletTransaction;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    List<WalletTransaction> findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(Long walletId, String transactionType);

    List<WalletTransaction> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);

    Page<WalletTransaction> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
