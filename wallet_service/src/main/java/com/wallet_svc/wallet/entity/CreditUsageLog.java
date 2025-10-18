package com.wallet_svc.wallet.entity;

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
@Table(name = "credit_usage_logs")
public class CreditUsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id", nullable = false)
    Integer userId;

    @Column(name = "wallet_transaction_id")
    Long walletTransactionId;

    @Column(name = "service_type", length = 50, nullable = false)
    String serviceType; // AI_SLIDE / AI_QUIZ / AI_WORKSHEET / STORAGE

    @Column(length = 100)
    String action; // "generate_slide", "upload_file"

    @Column(name = "resource_id", length = 100)
    String resourceId; // lesson_id, file_id

    @Column(name = "credits_used", nullable = false)
    Integer creditsUsed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    String metadata; // { num_slides, model_used, duration_ms, etc }

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
