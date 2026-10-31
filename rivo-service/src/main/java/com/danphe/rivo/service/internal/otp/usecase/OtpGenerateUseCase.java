package com.danphe.rivo.service.internal.otp.usecase;

import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.service.api.UseCase;
import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.common.otp.OtpDomain;
import com.danphe.rivo.repository.api.ClientOtpRepository;
import com.danphe.rivo.service.api.OtpSender;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

public class OtpGenerateUseCase implements UseCase<OtpGenerateCommand, String> {

  private final ClientOtpRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final OtpSender otpSender;
  private final SecureRandom secureRandom = new SecureRandom();

  public OtpGenerateUseCase(
      ClientOtpRepository repository,
      PasswordEncoder passwordEncoder,
      OtpSender otpSender) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
    this.otpSender = otpSender;
  }

  @Override
  public String execute(OtpGenerateCommand request) {
    validate(request);
    return process(request);
  }

  protected void validate(OtpGenerateCommand request) {
    String normalizedIdentifier = request.identifier().trim().toLowerCase();
    Optional<OtpDomain> existing = repository.findLatestActiveByIdentifierAndPurpose(
        normalizedIdentifier, request.purpose().name());

    if (existing.isPresent()) {
      OtpDomain otp = existing.get();
      if (otp.getResendAvailableAt() != null && LocalDateTime.now().isBefore(otp.getResendAvailableAt())) {
        long seconds = Duration.between(LocalDateTime.now(), otp.getResendAvailableAt()).getSeconds() + 1;
        throw new AppException("Please wait " + seconds + " seconds before requesting a new OTP.");
      }
    }
  }

  protected String process(OtpGenerateCommand request) {
    String normalizedIdentifier = request.identifier().trim().toLowerCase();
    repository.invalidateExistingByIdentifierAndPurpose(normalizedIdentifier, request.purpose().name());

    int code = 100000 + secureRandom.nextInt(900000);
    String plainOtp = String.valueOf(code);
    String hashedOtp = passwordEncoder.encode(plainOtp);

    OtpDomain domain = new OtpDomain();
    domain.setId(StringUtils.generateUUID());
    domain.setIdentifier(normalizedIdentifier);
    domain.setOtpHash(hashedOtp);
    domain.setPurpose(request.purpose().name());
    domain.setAttempts(0);
    domain.setMaxAttempts(5);
    domain.setResendAvailableAt(LocalDateTime.now().plusSeconds(60));
    domain.setExpiresAt(LocalDateTime.now().plusMinutes(5));
    domain.setConsumed(false);
    domain.setCreatedOn(LocalDateTime.now());
    domain.setCreatedBy("SYSTEM");
    domain.setLastModifiedOn(LocalDateTime.now());
    domain.setLastModifiedBy("SYSTEM");
    domain.setActive(true);
    domain.setDeleted(false);
    domain.setVersion(1L);

    repository.insert(domain);
    otpSender.sendOtp(normalizedIdentifier, plainOtp, request.purpose());

    return plainOtp;
  }
}
