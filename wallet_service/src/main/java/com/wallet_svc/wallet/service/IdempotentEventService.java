package com.wallet_svc.wallet.service;

import com.wallet_svc.wallet.entity.ProcessedEvent;
import com.wallet_svc.wallet.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Idempotent Event Processing Service
 *
 * Provides idempotency guarantees for event processing:
 * - Deduplicates events based on event ID
 * - Uses SHA-256 hash for additional payload verification
 * - Tracks processing history
 *
 * Critical for Saga Pattern to handle:
 * - Kafka redelivery
 * - Service restart
 * - Network retries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotentEventService {

    private final ProcessedEventRepository processedEventRepository;

    /**
     * Check if event has already been processed
     * This is the idempotency check
     */
    @Transactional(readOnly = true)
    public boolean isEventProcessed(String eventId, String eventType) {
        boolean exists = processedEventRepository.existsByEventIdAndEventType(eventId, eventType);
        if (exists) {
            log.info("Event already processed: {} (type: {}), skipping", eventId, eventType);
        }
        return exists;
    }

    /**
     * Record event as processed
     * Use REQUIRES_NEW to ensure this commits even if parent transaction fails
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markEventAsProcessed(String eventId, String eventType, String sourceService,
                                     String payload, String result, String details) {
        try {
            String payloadHash = generateHash(payload);

            ProcessedEvent processedEvent = ProcessedEvent.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .sourceService(sourceService)
                    .payloadHash(payloadHash)
                    .processingResult(result)
                    .resultDetails(details)
                    .build();

            processedEventRepository.save(processedEvent);
            log.debug("Marked event as processed: {} (type: {})", eventId, eventType);

        } catch (Exception e) {
            log.error("Failed to mark event as processed: {}", eventId, e);
            // Don't throw - this is a tracking failure, not a processing failure
        }
    }

    /**
     * Convenience method for successful processing
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markEventAsSuccess(String eventId, String eventType, String sourceService, String payload) {
        markEventAsProcessed(eventId, eventType, sourceService, payload, "SUCCESS", null);
    }

    /**
     * Convenience method for failed processing
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markEventAsFailed(String eventId, String eventType, String sourceService,
                                  String payload, String errorMessage) {
        markEventAsProcessed(eventId, eventType, sourceService, payload, "FAILED", errorMessage);
    }

    /**
     * Generate SHA-256 hash of payload for deduplication
     */
    private String generateHash(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error generating hash", e);
            return null;
        }
    }

    /**
     * Cleanup old processed events (keep for 30 days for audit)
     * Run daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldProcessedEvents() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            List<ProcessedEvent> oldEvents = processedEventRepository.findOldProcessedEvents(cutoffDate);

            if (!oldEvents.isEmpty()) {
                processedEventRepository.deleteAll(oldEvents);
                log.info("Cleaned up {} old processed events", oldEvents.size());
            }
        } catch (Exception e) {
            log.error("Error cleaning up old processed events", e);
        }
    }

    /**
     * Get recent processing failures for monitoring/alerting
     */
    @Transactional(readOnly = true)
    public List<ProcessedEvent> getRecentFailures(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return processedEventRepository.findRecentFailures(since);
    }
}

