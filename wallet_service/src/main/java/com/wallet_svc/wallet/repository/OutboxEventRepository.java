package com.wallet_svc.wallet.repository;

import com.wallet_svc.wallet.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("SELECT e FROM OutboxEvent e WHERE " +
           "(e.status = 'PENDING' OR (e.status = 'FAILED' AND e.nextRetryAt <= :now)) " +
           "AND e.retryCount < e.maxRetry " +
           "ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingEvents(LocalDateTime now);

    @Query("SELECT e FROM OutboxEvent e WHERE " +
           "e.status = 'PUBLISHED' AND e.publishedAt < :cutoffDate")
    List<OutboxEvent> findOldPublishedEvents(LocalDateTime cutoffDate);

    List<OutboxEvent> findByAggregateIdAndAggregateType(String aggregateId, String aggregateType);
}

