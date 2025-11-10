package com.wallet_svc.wallet.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Outbox Pattern Implementation for Wallet Service
 * Ensures event publishing is atomic with business transaction
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "outbox_events",
        indexes = {
            @Index(name = "idx_outbox_status_created", columnList = "status,created_at"),
            @Index(name = "idx_outbox_event_type", columnList = "event_type")
        })
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    String aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    String aggregateType;

    @Column(name = "event_type", nullable = false, length = 100)
    String eventType;

    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    String payload;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    String status = "PENDING";

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    Integer retryCount = 0;

    @Column(name = "max_retry", nullable = false)
    @Builder.Default
    Integer maxRetry = 5;

    @Column(name = "last_error", columnDefinition = "TEXT")
    String lastError;

    @Column(name = "published_at")
    LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "next_retry_at")
    LocalDateTime nextRetryAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public void scheduleNextRetry() {
        this.retryCount++;
        long secondsDelay = (long) Math.pow(2, this.retryCount);
        this.nextRetryAt = LocalDateTime.now().plusSeconds(secondsDelay);
    }

    public boolean canRetry() {
        return this.retryCount < this.maxRetry;
    }
}
