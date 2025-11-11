package com.wallet_svc.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenResponse {
    @JsonProperty("user_id")
    String userId;

    @JsonProperty("token_before")
    Integer tokenBefore;

    @JsonProperty("token_after")
    Integer tokenAfter;

    @JsonProperty("tokens_deducted")
    Integer tokensDeducted;

    @JsonProperty("transaction_id")
    Long transactionId;

    String status;

    String message;
}
