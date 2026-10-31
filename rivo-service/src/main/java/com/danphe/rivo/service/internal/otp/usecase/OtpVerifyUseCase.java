package com.danphe.rivo.service.internal.otp.usecase;

import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.service.api.UseCase;
import com.danphe.rivo.common.otp.OtpDomain;
import com.danphe.rivo.repository.api.ClientOtpRepository;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;

public class OtpVerifyUseCase implements UseCase<OtpVerifyCommand, Boolean> {

  private final ClientOtpRepository repository;
  private final PasswordEncoder passwordEncoder;

  public OtpVerifyUseCase(ClientOtpRepository repository, PasswordEncoder passwordEncoder) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Boolean execute(OtpVerifyCommand request) {
    validate(request);
    return process(request);
  }

  protected void validate(OtpVerifyCommand request) {
    String normalizedIdentifier = request.identifier().trim().toLowerCase();
    OtpDomain otp = repository.findLatestActiveByIdentifierAndPurpose(
        normalizedIdentifier, request.purpose().name())
        .orElseThrow(() -> new AppException("No active OTP request found. Please request a new OTP."));

    if (otp.getExpiresAt() != null && otp.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new AppException("OTP has expired. Please request a new OTP.");
    }

    if (otp.getAttempts() >= otp.getMaxAttempts()) {
      throw new AppException("Maximum verification attempts exceeded. Please request a new OTP.");
    }
  }

  protected Boolean process(OtpVerifyCommand request) {
    String normalizedIdentifier = request.identifier().trim().toLowerCase();
    OtpDomain otp = repository.findLatestActiveByIdentifierAndPurpose(
        normalizedIdentifier, request.purpose().name())
        .orElseThrow(() -> new AppException("No active OTP request found. Please request a new OTP."));

    if (!passwordEncoder.matches(request.plainOtp().trim(), otp.getOtpHash())) {
      int updatedAttempts = otp.getAttempts() + 1;
      otp.setAttempts(updatedAttempts);
      otp.setLastModifiedOn(LocalDateTime.now());
      otp.setLastModifiedBy("SYSTEM");
      if (updatedAttempts >= otp.getMaxAttempts()) {
        otp.setActive(false);
      }
      repository.update(otp);

      int remaining = otp.getMaxAttempts() - updatedAttempts;
      if (remaining <= 0) {
        throw new AppException("Maximum verification attempts exceeded. Please request a new OTP.");
      } else {
        throw new AppException("Invalid OTP. " + remaining + " attempt(s) remaining.");
      }
    }

    otp.setConsumed(true);
    otp.setConsumedAt(LocalDateTime.now());
    otp.setLastModifiedOn(LocalDateTime.now());
    otp.setLastModifiedBy("SYSTEM");
    repository.update(otp);

    return true;
  }
}
