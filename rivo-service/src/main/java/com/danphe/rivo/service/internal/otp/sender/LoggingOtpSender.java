package com.danphe.rivo.service.internal.otp.sender;

import com.danphe.rivo.common.otp.OtpPurpose;
import com.danphe.rivo.service.api.OtpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("loggingOtpSender")
public class LoggingOtpSender implements OtpSender {

  private static final Logger log = LoggerFactory.getLogger(LoggingOtpSender.class);

  @Override
  public void sendOtp(String identifier, String plainOtp, OtpPurpose purpose) {
    log.info("OTP verification code for [{}] (purpose: {}): {}", identifier, purpose, plainOtp);
  }
}
