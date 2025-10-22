package com.wallet_svc.wallet.event.payload;

import java.math.BigDecimal;
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
public class CreditsRefundedEvent {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("wallet_id")
    private Long walletId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("reference_type")
    private String referenceType;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("original_transaction_id")
    private Long originalTransactionId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
