package com.wallet_svc.wallet.dto.request;

import java.math.BigDecimal;

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
public class AdjustmentRequest {
    @NotNull(message = "User ID is required")
    @JsonProperty("user_id")
    String userId;

    @NotNull(message = "Amount is required")
    BigDecimal amount; // Can be positive or negative

    String description;

    @JsonProperty("processed_by")
    Integer processedBy; // Admin user ID

    String metadata;
}
