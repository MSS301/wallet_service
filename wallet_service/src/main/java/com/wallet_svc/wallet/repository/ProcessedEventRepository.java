package com.wallet_svc.wallet.repository;

import com.wallet_svc.wallet.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    /**
     * Check if an event has already been processed (idempotency check)
     */
    boolean existsByEventIdAndEventType(String eventId, String eventType);

    /**
     * Get processed event details
     */
    Optional<ProcessedEvent> findByEventIdAndEventType(String eventId, String eventType);

    /**
     * Find old processed events for cleanup (after 30 days)
     */
    @Query("SELECT e FROM ProcessedEvent e WHERE e.processedAt < :cutoffDate")
    List<ProcessedEvent> findOldProcessedEvents(LocalDateTime cutoffDate);

    /**
     * Find recent processing failures for monitoring
     */
    @Query("SELECT e FROM ProcessedEvent e WHERE " +
           "e.processingResult = 'FAILED' AND e.processedAt > :since " +
           "ORDER BY e.processedAt DESC")
    List<ProcessedEvent> findRecentFailures(LocalDateTime since);
}

