package com.wallet_svc.wallet.dto.request;

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
public class DeductTokenRequest {
    @NotNull(message = "User ID is required")
    @JsonProperty("user_id")
    String userId;

    @NotNull(message = "Token amount is required")
    @Positive(message = "Token amount must be positive")
    Integer tokens;

    String description;

    @JsonProperty("reference_type")
    @Builder.Default
    String referenceType = "AI_GENERATION";

    @JsonProperty("reference_id")
    String referenceId; // prompt ID or conversation ID
}
