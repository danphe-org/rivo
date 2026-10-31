package com.danphe.rivo.common.otp;

import com.danphe.rivo.common.library.base.service.api.AbstractDomain;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OtpDomain extends AbstractDomain {
  private String identifier;
  private String otpHash;
  private String purpose;
  @Builder.Default
  private int attempts = 0;
  @Builder.Default
  private int maxAttempts = 5;
  private LocalDateTime resendAvailableAt;
  private LocalDateTime expiresAt;
  @Builder.Default
  private boolean consumed = false;
  private LocalDateTime consumedAt;
}
