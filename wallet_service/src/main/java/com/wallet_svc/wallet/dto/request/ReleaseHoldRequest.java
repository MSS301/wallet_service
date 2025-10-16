package com.wallet_svc.wallet.dto.request;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReleaseHoldRequest {
	@NotNull(message = "Hold ID is required")
	@JsonProperty("hold_id")
	Long holdId;
}
