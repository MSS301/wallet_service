package com.wallet_svc.wallet.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "daily_wallet_stats")
public class DailyWalletStats {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@Column(nullable = false, unique = true)
	LocalDate date;

	@Column(name = "total_wallets")
	@Builder.Default
	Integer totalWallets = 0;

	@Column(name = "active_wallets")
	@Builder.Default
	Integer activeWallets = 0;

	@Column(name = "total_balance", precision = 15, scale = 2)
	@Builder.Default
	BigDecimal totalBalance = BigDecimal.ZERO;

	@Column(name = "total_spent", precision = 15, scale = 2)
	@Builder.Default
	BigDecimal totalSpent = BigDecimal.ZERO;

	@Column(name = "total_earned", precision = 15, scale = 2)
	@Builder.Default
	BigDecimal totalEarned = BigDecimal.ZERO;

	@Column(name = "total_transactions")
	@Builder.Default
	Integer totalTransactions = 0;

	@Column(name = "avg_transaction_value", precision = 15, scale = 2)
	BigDecimal avgTransactionValue;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	String metadata;

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
