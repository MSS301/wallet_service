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
public class CreditsUsedEvent {
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("service_type")
    private String serviceType;

    @JsonProperty("credits")
    private BigDecimal credits;

    @JsonProperty("resource_id")
    private String resourceId;

    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
