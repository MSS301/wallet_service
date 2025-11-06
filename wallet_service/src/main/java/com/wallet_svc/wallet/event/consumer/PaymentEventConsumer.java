package com.wallet_svc.wallet.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet_svc.wallet.dto.request.TopUpRequest;
import com.wallet_svc.wallet.event.payload.BonusGrantedEvent;
import com.wallet_svc.wallet.event.payload.PaymentCompletedEvent;
import com.wallet_svc.wallet.service.IdempotentEventService;
import com.wallet_svc.wallet.service.WalletService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Payment Event Consumer with Idempotency Support
 *
 * Implements Saga Pattern with:
 * - Idempotent event processing (prevents duplicate topups)
 * - Transaction management
 * - Error handling with compensation
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentEventConsumer {
    WalletService walletService;
    ObjectMapper objectMapper;
    IdempotentEventService idempotentEventService;

    /**
     * Handle Payment Completed Event with Idempotency
     * This prevents duplicate topups if Kafka redelivers the same event
     */
    @KafkaListener(
            topics = "payment.completed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentCompleted(String eventJson) {
        String eventId = null;
        try {
            // Deserialize JSON string to PaymentCompletedEvent
            PaymentCompletedEvent event = objectMapper.readValue(eventJson, PaymentCompletedEvent.class);

            // Use paymentId as unique event identifier (convert Long to String)
            eventId = "payment-" + event.getPaymentId();

            // ===== IDEMPOTENCY CHECK =====
            if (idempotentEventService.isEventProcessed(eventId, "payment.completed")) {
                log.warn("Event already processed, skipping: {}", eventId);
                return; // Skip duplicate event
            }

            log.info("Received payment.completed event for user: {} - amount: {} - paymentId: {}",
                    event.getUserId(), event.getAmount(), event.getPaymentId());

            // Process the event
            TopUpRequest request = TopUpRequest.builder()
                    .userId(event.getUserId())
                    .amount(event.getAmount())
                    .referenceType("PAYMENT")
                    .referenceId(String.valueOf(event.getPaymentId()))  // Convert Long to String
                    .description("Payment top-up via " + event.getPaymentMethod())
                    .metadata("{\"currency\":\"" + event.getCurrency() + "\",\"method\":\"" + event.getPaymentMethod()
                            + "\",\"orderId\":\"" + event.getOrderId() + "\"}")
                    .build();

            walletService.topUp(request);

            // Mark as successfully processed
            idempotentEventService.markEventAsSuccess(eventId, "payment.completed", "payment-service", eventJson);

            log.info("✅ Wallet topped up for user: {} - amount: {}", event.getUserId(), event.getAmount());

        } catch (Exception e) {
            log.error("❌ Failed to top up wallet for payment event", e);

            // Mark as failed for monitoring
            if (eventId != null) {
                idempotentEventService.markEventAsFailed(eventId, "payment.completed",
                        "payment-service", eventJson, e.getMessage());
            }

            // In production: Send to Dead Letter Queue for manual review
            // throw e; // Uncomment to trigger Kafka retry
        }
    }

    /**
     * Handle Bonus Granted Event with Idempotency
     */
    @KafkaListener(
            topics = "payment.bonus_granted",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleBonusGranted(String eventJson) {
        String eventId = null;
        try {
            // Deserialize JSON string to BonusGrantedEvent
            BonusGrantedEvent event = objectMapper.readValue(eventJson, BonusGrantedEvent.class);

            // Use referenceId as unique event identifier
            eventId = "bonus-" + event.getReferenceId();

            // ===== IDEMPOTENCY CHECK =====
            if (idempotentEventService.isEventProcessed(eventId, "payment.bonus_granted")) {
                log.warn("Event already processed, skipping: {}", eventId);
                return;
            }

            log.info("Received payment.bonus_granted event for user: {} - amount: {}",
                    event.getUserId(), event.getAmount());

            TopUpRequest request = TopUpRequest.builder()
                    .userId(event.getUserId())
                    .amount(event.getAmount())
                    .referenceType("BONUS")
                    .referenceId(event.getReferenceId())
                    .description("Bonus credits: " + event.getReason())
                    .metadata("{\"reason\":\"" + event.getReason() + "\"}")
                    .build();

            walletService.topUp(request);

            // Mark as successfully processed
            idempotentEventService.markEventAsSuccess(eventId, "payment.bonus_granted", "payment-service", eventJson);

            log.info("✅ Bonus credits added for user: {} - amount: {}", event.getUserId(), event.getAmount());

        } catch (Exception e) {
            log.error("❌ Failed to add bonus credits", e);

            if (eventId != null) {
                idempotentEventService.markEventAsFailed(eventId, "payment.bonus_granted",
                        "payment-service", eventJson, e.getMessage());
            }
        }
    }
}
