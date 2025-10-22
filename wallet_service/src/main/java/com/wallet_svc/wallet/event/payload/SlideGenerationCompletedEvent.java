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
public class SlideGenerationCompletedEvent {
    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("slide_id")
    private String slideId;

    @JsonProperty("credits_used")
    private BigDecimal creditsUsed;

    @JsonProperty("hold_id")
    private Long holdId;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
