package com.wallet_svc.wallet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wallet_svc.wallet.entity.WalletHold;

@Repository
public interface WalletHoldRepository extends JpaRepository<WalletHold, Long> {
    List<WalletHold> findByWalletIdAndStatus(Long walletId, String status);

    List<WalletHold> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);
}
