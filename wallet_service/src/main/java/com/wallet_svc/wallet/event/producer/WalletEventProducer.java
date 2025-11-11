package com.wallet_svc.wallet.event.producer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.wallet_svc.wallet.event.payload.*;

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

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("10.00");

    // ============ Balance Events ============

    public void publishWalletCreatedEvent(Long walletId, String userId) {
        WalletCreatedEvent event = WalletCreatedEvent.builder()
                .walletId(walletId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.created", event);
        log.info("Published wallet.created event for user: {}", userId);
    }

    public void publishBalanceUpdatedEvent(
            String userId, Long walletId, BigDecimal oldBalance, BigDecimal newBalance, String transactionType) {
        BalanceUpdatedEvent event = BalanceUpdatedEvent.builder()
                .userId(userId)
                .walletId(walletId)
                .oldBalance(oldBalance)
                .newBalance(newBalance)
                .transactionType(transactionType)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.balance_updated", event);
        log.info("Published wallet.balance_updated event for user: {} - {} to {}", userId, oldBalance, newBalance);

        // Check if balance is low
        if (newBalance.compareTo(LOW_BALANCE_THRESHOLD) <= 0) {
            publishBalanceLowEvent(userId, walletId, newBalance);
        }
    }

    public void publishBalanceLowEvent(String userId, Long walletId, BigDecimal balance) {
        BalanceLowEvent event = BalanceLowEvent.builder()
                .userId(userId)
                .walletId(walletId)
                .balance(balance)
                .threshold(LOW_BALANCE_THRESHOLD)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.balance_low", event);
        log.warn("Published wallet.balance_low event for user: {} - balance: {}", userId, balance);
    }

    // ============ Transaction Events ============

    public void publishTransactionCreatedEvent(
            Long transactionId,
            String userId,
            Long walletId,
            String type,
            BigDecimal amount,
            String referenceType,
            String referenceId) {
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .transactionId(transactionId)
                .userId(userId)
                .walletId(walletId)
                .type(type)
                .amount(amount)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.transaction_created", event);
        log.info("Published wallet.transaction_created event - transaction: {}, type: {}", transactionId, type);
    }

    public void publishCreditsHeldEvent(
            String userId,
            Long walletId,
            BigDecimal amount,
            String referenceId,
            String referenceType,
            Long holdId,
            LocalDateTime expiresAt) {
        CreditsHeldEvent event = CreditsHeldEvent.builder()
                .userId(userId)
                .walletId(walletId)
                .amount(amount)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .holdId(holdId)
                .expiresAt(expiresAt)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.credits_held", event);
        log.info("Published wallet.credits_held event for user: {} - amount: {}, hold: {}", userId, amount, holdId);
    }

    public void publishCreditsChargedEvent(
            String userId,
            Long walletId,
            BigDecimal amount,
            String referenceId,
            String referenceType,
            BigDecimal balanceAfter,
            Long holdId) {
        CreditsChargedEvent event = CreditsChargedEvent.builder()
                .userId(userId)
                .walletId(walletId)
                .amount(amount)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .balanceAfter(balanceAfter)
                .holdId(holdId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.credits_charged", event);
        log.info(
                "Published wallet.credits_charged event for user: {} - amount: {}, balance: {}",
                userId,
                amount,
                balanceAfter);
    }

    public void publishCreditsRefundedEvent(
            String userId,
            Long walletId,
            BigDecimal amount,
            String referenceId,
            String referenceType,
            String reason,
            Long originalTransactionId) {
        CreditsRefundedEvent event = CreditsRefundedEvent.builder()
                .userId(userId)
                .walletId(walletId)
                .amount(amount)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .reason(reason)
                .originalTransactionId(originalTransactionId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.credits_refunded", event);
        log.info("Published wallet.credits_refunded event for user: {} - amount: {}", userId, amount);
    }

    public void publishHoldExpiredEvent(
            String userId, Long walletId, Long holdId, BigDecimal amount, String referenceId) {
        HoldExpiredEvent event = HoldExpiredEvent.builder()
                .userId(userId)
                .walletId(walletId)
                .holdId(holdId)
                .amount(amount)
                .referenceId(referenceId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.hold_expired", event);
        log.warn("Published wallet.hold_expired event for user: {} - hold: {}, amount: {}", userId, holdId, amount);
    }

    // ============ Usage Events ============

    public void publishCreditsUsedEvent(
            String userId, String serviceType, BigDecimal credits, String resourceId, String metadata) {
        CreditsUsedEvent event = CreditsUsedEvent.builder()
                .userId(userId)
                .serviceType(serviceType)
                .credits(credits)
                .resourceId(resourceId)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.credits_used", event);
        log.info(
                "Published wallet.credits_used event for user: {} - service: {}, credits: {}",
                userId,
                serviceType,
                credits);
    }

    // ============ Token Events ============

    public void publishTokenDeductedEvent(
            String userId,
            Long walletId,
            Integer tokensDeducted,
            Integer tokenBefore,
            Integer tokenAfter,
            String referenceId,
            String referenceType) {
        TokenDeductedEvent event = TokenDeductedEvent.builder()
                .userId(userId)
                .walletId(walletId)
                .tokensDeducted(tokensDeducted)
                .tokenBefore(tokenBefore)
                .tokenAfter(tokenAfter)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("wallet.token_deducted", event);
        log.info(
                "Published wallet.token_deducted event for user: {} - tokens: {}, remaining: {}",
                userId,
                tokensDeducted,
                tokenAfter);
    }
}
