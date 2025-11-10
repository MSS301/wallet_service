package com.wallet_svc.wallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    String userId;

    @Column(precision = 15, scale = 2, nullable = false)
    @Builder.Default
    BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "total_spent", precision = 15, scale = 2)
    @Builder.Default
    BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "total_earned", precision = 15, scale = 2)
    @Builder.Default
    BigDecimal totalEarned = BigDecimal.ZERO;

    @Column(name = "total_refunded", precision = 15, scale = 2)
    @Builder.Default
    BigDecimal totalRefunded = BigDecimal.ZERO;

    @Column(length = 10)
    @Builder.Default
    String currency = "VND";

    @Column(length = 50)
    @Builder.Default
    String status = "ACTIVE"; // ACTIVE / SUSPENDED / CLOSED

    @Column(name = "locked_balance", precision = 15, scale = 2)
    @Builder.Default
    BigDecimal lockedBalance = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "token")
    Integer token;

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
