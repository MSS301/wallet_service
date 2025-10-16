package com.wallet_svc.wallet.event.consumer;

import com.wallet_svc.wallet.event.payload.UserRegisteredEvent;
import com.wallet_svc.wallet.service.WalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserEventConsumer {
	WalletService walletService;

	@KafkaListener(topics = "user.registered", groupId = "${spring.kafka.consumer.group-id}")
	public void handleUserRegistered(UserRegisteredEvent event) {
		log.info("Received user.registered event for user: {}", event.getUserId());

		try {
			walletService.createWallet(event.getUserId());
			log.info("Wallet created for user: {}", event.getUserId());
		} catch (Exception e) {
			log.error("Failed to create wallet for user: {}", event.getUserId(), e);
			// In production, implement retry logic or dead letter queue
		}
	}
}
