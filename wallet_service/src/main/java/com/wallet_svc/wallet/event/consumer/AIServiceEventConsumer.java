package com.wallet_svc.wallet.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet_svc.wallet.dto.request.ChargeRequest;
import com.wallet_svc.wallet.dto.request.HoldRequest;
import com.wallet_svc.wallet.dto.request.ReleaseHoldRequest;
import com.wallet_svc.wallet.event.payload.SlideGenerationCompletedEvent;
import com.wallet_svc.wallet.event.payload.SlideGenerationFailedEvent;
import com.wallet_svc.wallet.event.payload.SlideGenerationRequestedEvent;
import com.wallet_svc.wallet.service.WalletService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AIServiceEventConsumer {
    WalletService walletService;
    ObjectMapper objectMapper;

    @KafkaListener(
            topics = "slide.generation_requested",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleSlideGenerationRequested(Object eventObject) {
        try {
            SlideGenerationRequestedEvent event =
                    objectMapper.convertValue(eventObject, SlideGenerationRequestedEvent.class);

            log.info(
                    "Received slide.generation_requested event for user: {} - credits: {}",
                    event.getUserId(),
                    event.getCreditsRequired());
            HoldRequest request = HoldRequest.builder()
                    .userId(event.getUserId())
                    .amount(event.getCreditsRequired())
                    .referenceType("SLIDE_GENERATION")
                    .referenceId(event.getRequestId())
                    .reason("Hold for slide generation - " + event.getSlideCount() + " slides")
                    .expirationMinutes(30) // 30 minutes expiration
                    .build();

            walletService.holdCredits(request);
            log.info(
                    "Credits held for user: {} - amount: {}, request: {}",
                    event.getUserId(),
                    event.getCreditsRequired(),
                    event.getRequestId());
        } catch (Exception e) {
            //            log.error("Failed to hold credits for user: {} - request: {}", event.getUserId(),
            // event.getRequestId(), e);
            // In production, publish failure event back to AI service
        }
    }

    @KafkaListener(
            topics = "slide.generation_completed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleSlideGenerationCompleted(SlideGenerationCompletedEvent event) {
        log.info(
                "Received slide.generation_completed event for user: {} - credits: {}",
                event.getUserId(),
                event.getCreditsUsed());

        try {
            ChargeRequest request = ChargeRequest.builder()
                    .userId(event.getUserId())
                    .amount(event.getCreditsUsed())
                    .holdId(event.getHoldId())
                    .referenceType("SLIDE_GENERATION")
                    .referenceId(event.getSlideId())
                    .description("Charged for slide generation")
                    .metadata("{\"request_id\":\"" + event.getRequestId() + "\"}")
                    .build();

            walletService.charge(request);
            log.info(
                    "Credits charged for user: {} - amount: {}, slide: {}",
                    event.getUserId(),
                    event.getCreditsUsed(),
                    event.getSlideId());
        } catch (Exception e) {
            log.error("Failed to charge credits for user: {} - slide: {}", event.getUserId(), event.getSlideId(), e);
            // In production, implement retry logic or dead letter queue
        }
    }

    @KafkaListener(
            topics = "slide.generation_failed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handleSlideGenerationFailed(SlideGenerationFailedEvent event) {
        log.info(
                "Received slide.generation_failed event for user: {} - hold: {}", event.getUserId(), event.getHoldId());

        try {
            ReleaseHoldRequest request =
                    ReleaseHoldRequest.builder().holdId(event.getHoldId()).build();

            walletService.releaseHold(request);
            log.info(
                    "Hold released for user: {} - hold: {}, reason: {}",
                    event.getUserId(),
                    event.getHoldId(),
                    event.getReason());
        } catch (Exception e) {
            log.error("Failed to release hold for failed slide generation", e);
            // In production, implement retry logic or dead letter queue
        }
    }
}
