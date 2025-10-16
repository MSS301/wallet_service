package com.wallet_svc.event;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreatedEvent {
	@JsonProperty("wallet_id")
	private Long walletId;

	@JsonProperty("user_id")
	private Integer userId;

	@JsonProperty("timestamp")
	private LocalDateTime timestamp;
}
