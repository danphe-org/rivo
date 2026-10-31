package com.danphe.rivo.repository.internal.otp;

import com.danphe.rivo.common.library.base.dao.entity.AbstractEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClientOtpEntity extends AbstractEntity {
  private String identifier;
  private String otpHash;
  private String purpose;
  private int attempts = 0;
  private int maxAttempts = 5;
  private LocalDateTime resendAvailableAt;
  private LocalDateTime expiresAt;
  private boolean consumed = false;
  private LocalDateTime consumedAt;
}
