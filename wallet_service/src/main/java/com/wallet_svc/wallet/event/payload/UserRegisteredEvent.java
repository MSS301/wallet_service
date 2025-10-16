package com.wallet_svc.wallet.event.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
	@JsonProperty("user_id")
	private Integer userId;

	@JsonProperty("email")
	private String email;

	@JsonProperty("username")
	private String username;

	@JsonProperty("timestamp")
	private LocalDateTime timestamp;
}
