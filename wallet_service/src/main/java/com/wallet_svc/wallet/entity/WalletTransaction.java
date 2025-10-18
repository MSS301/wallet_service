package com.wallet_svc.wallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "wallet_transactions")
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "wallet_id", nullable = false)
    Long walletId;

    @Column(name = "transaction_type", length = 50, nullable = false)
    String transactionType; // TOP_UP / CHARGE / REFUND / ADJUSTMENT / HOLD / RELEASE

    @Column(precision = 15, scale = 2, nullable = false)
    BigDecimal amount;

    @Column(name = "reference_type", length = 50)
    String referenceType; // AI_GENERATION / SUBSCRIPTION / BONUS / PROMOTION / PAYMENT

    @Column(name = "reference_id", length = 100)
    String referenceId;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(length = 50)
    @Builder.Default
    String status = "PENDING"; // PENDING / PROCESSING / SUCCESS / FAILED / CANCELLED / REVERSED

    @Column(name = "balance_before", precision = 15, scale = 2)
    BigDecimal balanceBefore;

    @Column(name = "balance_after", precision = 15, scale = 2)
    BigDecimal balanceAfter;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    String metadata;

    @Column(name = "related_transaction_id")
    Long relatedTransactionId;

    @Column(name = "processed_by")
    Integer processedBy; // admin user_id if manual

    @Column(name = "processed_at")
    LocalDateTime processedAt;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
}
