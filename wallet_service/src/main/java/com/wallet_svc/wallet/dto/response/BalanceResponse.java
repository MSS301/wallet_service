package com.wallet_svc.wallet.dto.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BalanceResponse {
	@JsonProperty("user_id")
	Integer userId;

	BigDecimal balance;

	@JsonProperty("locked_balance")
	BigDecimal lockedBalance;

	@JsonProperty("available_balance")
	BigDecimal availableBalance;

	String currency;
	String status;
}
