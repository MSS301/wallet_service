package com.wallet_svc.wallet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse {
	Long id;

	@JsonProperty("wallet_id")
	Long walletId;

	@JsonProperty("transaction_type")
	String transactionType;

	BigDecimal amount;

	@JsonProperty("reference_type")
	String referenceType;

	@JsonProperty("reference_id")
	String referenceId;

	String description;
	String status;

	@JsonProperty("balance_before")
	BigDecimal balanceBefore;

	@JsonProperty("balance_after")
	BigDecimal balanceAfter;

	String metadata;

	@JsonProperty("related_transaction_id")
	Long relatedTransactionId;

	@JsonProperty("processed_by")
	Integer processedBy;

	@JsonProperty("processed_at")
	LocalDateTime processedAt;

	@JsonProperty("created_at")
	LocalDateTime createdAt;

	@JsonProperty("updated_at")
	LocalDateTime updatedAt;
}
