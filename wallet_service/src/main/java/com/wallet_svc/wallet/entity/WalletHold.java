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
@Table(name = "wallet_holds")
public class WalletHold {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@Column(name = "wallet_id", nullable = false)
	Long walletId;

	@Column(precision = 15, scale = 2, nullable = false)
	BigDecimal amount;

	@Column(length = 100)
	String reason; // "AI_GENERATION_PENDING"

	@Column(name = "reference_type", length = 50)
	String referenceType;

	@Column(name = "reference_id", length = 100)
	String referenceId;

	@Column(length = 50)
	@Builder.Default
	String status = "ACTIVE"; // ACTIVE / RELEASED / EXPIRED

	@Column(name = "expires_at")
	LocalDateTime expiresAt;

	@Column(name = "released_at")
	LocalDateTime releasedAt;

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
