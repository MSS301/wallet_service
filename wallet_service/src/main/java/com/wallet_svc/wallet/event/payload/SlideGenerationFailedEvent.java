package com.wallet_svc.wallet.event.payload;

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
public class SlideGenerationFailedEvent {
    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("hold_id")
    private Long holdId;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
