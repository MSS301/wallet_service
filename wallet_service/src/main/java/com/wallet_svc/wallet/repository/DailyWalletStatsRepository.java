package com.wallet_svc.wallet.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wallet_svc.wallet.entity.DailyWalletStats;

@Repository
public interface DailyWalletStatsRepository extends JpaRepository<DailyWalletStats, Long> {
	Optional<DailyWalletStats> findByDate(LocalDate date);

	List<DailyWalletStats> findByDateBetweenOrderByDateDesc(LocalDate from, LocalDate to);
}
