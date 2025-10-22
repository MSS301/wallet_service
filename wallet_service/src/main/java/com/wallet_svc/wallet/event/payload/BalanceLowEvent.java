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
public class BalanceLowEvent {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("wallet_id")
    private Long walletId;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("threshold")
    private BigDecimal threshold;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
