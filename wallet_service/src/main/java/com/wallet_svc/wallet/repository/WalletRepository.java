package com.wallet_svc.wallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wallet_svc.wallet.entity.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
	Optional<Wallet> findByUserId(Integer userId);

	boolean existsByUserId(Integer userId);
}
