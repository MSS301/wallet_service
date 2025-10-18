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
public class UsageLogRequest {
    @NotNull(message = "User ID is required")
    @JsonProperty("user_id")
    Integer userId;

    @JsonProperty("wallet_transaction_id")
    Long walletTransactionId;

    @NotNull(message = "Service type is required")
    @JsonProperty("service_type")
    String serviceType; // AI_SLIDE / AI_QUIZ / AI_WORKSHEET / STORAGE

    String action; // "generate_slide", "upload_file"

    @JsonProperty("resource_id")
    String resourceId; // lesson_id, file_id

    @NotNull(message = "Credits used is required")
    @Positive(message = "Credits used must be positive")
    @JsonProperty("credits_used")
    Integer creditsUsed;

    String metadata;
}
