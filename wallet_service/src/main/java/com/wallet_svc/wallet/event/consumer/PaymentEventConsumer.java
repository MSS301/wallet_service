package com.wallet_svc.wallet.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet_svc.wallet.dto.request.TopUpRequest;
import com.wallet_svc.wallet.event.payload.BonusGrantedEvent;
import com.wallet_svc.wallet.event.payload.PaymentCompletedEvent;
import com.wallet_svc.wallet.service.WalletService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentEventConsumer {
    WalletService walletService;
    ObjectMapper objectMapper;

    @KafkaListener(
            topics = "payment.completed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentCompleted(Object eventObject) {
        try {
            PaymentCompletedEvent event = objectMapper.convertValue(eventObject, PaymentCompletedEvent.class);

            log.info(
                    "Received payment.completed event for user: {} - amount: {}", event.getUserId(), event.getAmount());
            TopUpRequest request = TopUpRequest.builder()
                    .userId(event.getUserId())
                    .amount(event.getAmount())
                    .referenceType("PAYMENT")
                    .referenceId(event.getPaymentId())
                    .description("Payment top-up via " + event.getPaymentMethod())
                    .metadata("{\"currency\":\"" + event.getCurrency() + "\",\"method\":\"" + event.getPaymentMethod()
                            + "\"}")
                    .build();

            walletService.topUp(request);
            log.info("Wallet topped up for user: {} - amount: {}", event.getUserId(), event.getAmount());
        } catch (Exception e) {
            log.error("Failed to top up wallet for payment event", e);
            // In production, implement retry logic or dead letter queue
        }
    }

    @KafkaListener(
            topics = "payment.bonus_granted",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleBonusGranted(Object eventObject) {
        try {
            BonusGrantedEvent event = objectMapper.convertValue(eventObject, BonusGrantedEvent.class);

            log.info(
                    "Received payment.bonus_granted event for user: {} - amount: {}",
                    event.getUserId(),
                    event.getAmount());
            TopUpRequest request = TopUpRequest.builder()
                    .userId(event.getUserId())
                    .amount(event.getAmount())
                    .referenceType("BONUS")
                    .referenceId(event.getReferenceId())
                    .description("Bonus credits: " + event.getReason())
                    .metadata("{\"reason\":\"" + event.getReason() + "\"}")
                    .build();

            walletService.topUp(request);
            log.info("Bonus credits added for user: {} - amount: {}", event.getUserId(), event.getAmount());
        } catch (Exception e) {
            log.error("Failed to add bonus credits", e);
            // In production, implement retry logic or dead letter queue
        }
    }
}
