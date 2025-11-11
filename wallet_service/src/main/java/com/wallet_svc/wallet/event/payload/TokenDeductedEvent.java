package com.wallet_svc.wallet.event.payload;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenDeductedEvent {
    String userId;
    Long walletId;
    Integer tokensDeducted;
    Integer tokenBefore;
    Integer tokenAfter;
    String referenceId;
    String referenceType;
    LocalDateTime timestamp;
}
