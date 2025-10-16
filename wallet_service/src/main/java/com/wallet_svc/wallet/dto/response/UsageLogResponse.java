package com.wallet_svc.wallet.dto.response;

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
public class UsageLogResponse {
	Long id;

	@JsonProperty("user_id")
	Integer userId;

	@JsonProperty("wallet_transaction_id")
	Long walletTransactionId;

	@JsonProperty("service_type")
	String serviceType;

	String action;

	@JsonProperty("resource_id")
	String resourceId;

	@JsonProperty("credits_used")
	Integer creditsUsed;

	String metadata;

	@JsonProperty("created_at")
	LocalDateTime createdAt;
}
