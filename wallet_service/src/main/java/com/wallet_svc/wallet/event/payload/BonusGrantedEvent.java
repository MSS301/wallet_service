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
public class BonusGrantedEvent {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
