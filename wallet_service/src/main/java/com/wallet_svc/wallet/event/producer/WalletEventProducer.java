package com.wallet_svc.wallet.event.producer;

import java.time.LocalDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.wallet_svc.wallet.event.payload.WalletCreatedEvent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WalletEventProducer {
    KafkaTemplate<String, Object> kafkaTemplate;

    public void publishWalletCreatedEvent(Long walletId, String userId) {
        WalletCreatedEvent event = WalletCreatedEvent.builder()
                .walletId(walletId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.created", event);
        log.info("Published wallet.created event for user: {}", userId);
    }
}
