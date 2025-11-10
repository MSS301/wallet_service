package com.wallet_svc.wallet.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet_svc.wallet.entity.OutboxEvent;
import com.wallet_svc.wallet.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Outbox Event Publisher for Wallet Service
 * Ensures reliable event publishing with retry mechanism
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherService {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(
            fixedDelayString = "${outbox.publisher.fixed-delay:30000}",
            initialDelayString = "${outbox.publisher.initial-delay:10000}")
    @Transactional
    public void publishPendingEvents() {
        try {
            List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents(LocalDateTime.now());

            if (pendingEvents.isEmpty()) {
                return;
            }

            log.info("Found {} pending outbox events to publish", pendingEvents.size());

            for (OutboxEvent event : pendingEvents) {
                try {
                    publishEvent(event);
                } catch (Exception e) {
                    handlePublishFailure(event, e);
                }
            }
        } catch (Exception e) {
            log.error("Error in outbox publisher job", e);
        }
    }

    private void publishEvent(OutboxEvent event) {
        try {
            Object payload = objectMapper.readValue(event.getPayload(), Object.class);

            kafkaTemplate
                    .send(event.getEventType(), event.getAggregateId(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            markAsPublished(event);
                            log.info("Published outbox event {} to topic {}", event.getId(), event.getEventType());
                        } else {
                            log.error("Failed to publish outbox event {}: {}", event.getId(), ex.getMessage());
                            throw new RuntimeException("Kafka publish failed", ex);
                        }
                    });

        } catch (Exception e) {
            log.error("Error publishing outbox event {}: {}", event.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Transactional
    public void markAsPublished(OutboxEvent event) {
        event.setStatus("PUBLISHED");
        event.setPublishedAt(LocalDateTime.now());
        outboxRepository.save(event);
    }

    @Transactional
    public void handlePublishFailure(OutboxEvent event, Exception e) {
        event.setLastError(e.getMessage());

        if (event.canRetry()) {
            event.setStatus("FAILED");
            event.scheduleNextRetry();
            log.warn("Outbox event {} failed, will retry at {}", event.getId(), event.getNextRetryAt());
        } else {
            event.setStatus("FAILED");
            log.error("Outbox event {} exceeded max retries, marking as FAILED permanently", event.getId());
        }

        outboxRepository.save(event);
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldEvents() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            List<OutboxEvent> oldEvents = outboxRepository.findOldPublishedEvents(cutoffDate);

            if (!oldEvents.isEmpty()) {
                outboxRepository.deleteAll(oldEvents);
                log.info("Cleaned up {} old outbox events", oldEvents.size());
            }
        } catch (Exception e) {
            log.error("Error cleaning up old outbox events", e);
        }
    }
}
