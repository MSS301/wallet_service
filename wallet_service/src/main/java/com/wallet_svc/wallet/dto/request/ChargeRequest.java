package com.wallet_svc.wallet.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChargeRequest {
    @NotNull(message = "User ID is required")
    @JsonProperty("user_id")
    String userId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount;

    String description;

    @JsonProperty("reference_type")
    String referenceType;

    @JsonProperty("reference_id")
    String referenceId;

    String metadata;

    @JsonProperty("hold_id")
    Long holdId; // Optional: if charging from a hold
}
