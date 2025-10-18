package com.wallet_svc.wallet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wallet_svc.wallet.entity.CreditPackage;

@Repository
public interface CreditPackageRepository extends JpaRepository<CreditPackage, Long> {
    Optional<CreditPackage> findByCode(String code);

    List<CreditPackage> findByIsActiveTrueOrderByDisplayOrder();
}
