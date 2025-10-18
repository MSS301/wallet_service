package com.wallet_svc.wallet.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wallet_svc.wallet.entity.CreditUsageLog;

@Repository
public interface CreditUsageLogRepository extends JpaRepository<CreditUsageLog, Long> {
    Page<CreditUsageLog> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    List<CreditUsageLog> findByUserIdAndCreatedAtBetween(Integer userId, LocalDateTime from, LocalDateTime to);

    Page<CreditUsageLog> findByServiceTypeOrderByCreatedAtDesc(String serviceType, Pageable pageable);
}
