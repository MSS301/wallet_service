package com.wallet_svc.wallet.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WalletResponse {
    Long id;

    @JsonProperty("user_id")
    String userId;

    BigDecimal balance;

    @JsonProperty("total_spent")
    BigDecimal totalSpent;

    @JsonProperty("total_earned")
    BigDecimal totalEarned;

    @JsonProperty("total_refunded")
    BigDecimal totalRefunded;

    String currency;
    String status;

    @JsonProperty("locked_balance")
    BigDecimal lockedBalance;

    @JsonProperty("created_at")
    LocalDateTime createdAt;

    @JsonProperty("updated_at")
    LocalDateTime updatedAt;
}
