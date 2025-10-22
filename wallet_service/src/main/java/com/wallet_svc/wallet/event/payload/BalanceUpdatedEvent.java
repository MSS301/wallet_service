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
public class BalanceUpdatedEvent {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("wallet_id")
    private Long walletId;

    @JsonProperty("old_balance")
    private BigDecimal oldBalance;

    @JsonProperty("new_balance")
    private BigDecimal newBalance;

    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
