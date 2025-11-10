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
public class PaymentCompletedEvent {
    @JsonProperty("payment_id")
    private Long paymentId; // Changed from String to Long to match payment service

    @JsonProperty("order_id")
    private Long orderId; // Added to match payment service event

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("credits")
    private Integer credits;
}
