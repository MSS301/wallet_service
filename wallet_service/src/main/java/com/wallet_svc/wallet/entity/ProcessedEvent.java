package com.wallet_svc.wallet.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Idempotency Key tracking for event processing
 * Prevents duplicate event processing when same event is delivered multiple times
 * Critical for Saga Pattern eventual consistency
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "processed_events",
        indexes = {
            @Index(name = "idx_event_id_type", columnList = "event_id,event_type", unique = true),
            @Index(name = "idx_processed_at", columnList = "processed_at")
        })
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    String eventId; // Unique identifier from source event

    @Column(name = "event_type", nullable = false, length = 100)
    String eventType; // e.g., "payment.completed"

    @Column(name = "source_service", length = 50)
    String sourceService; // e.g., "payment-service"

    @Column(name = "payload_hash", length = 64)
    String payloadHash; // SHA-256 hash of payload for additional verification

    @Column(name = "processed_at", nullable = false)
    @Builder.Default
    LocalDateTime processedAt = LocalDateTime.now();

    @Column(name = "processing_result", length = 20)
    String processingResult; // SUCCESS / FAILED / SKIPPED

    @Column(name = "result_details", columnDefinition = "TEXT")
    String resultDetails;

    @PrePersist
    public void prePersist() {
        if (this.processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
    }
}
