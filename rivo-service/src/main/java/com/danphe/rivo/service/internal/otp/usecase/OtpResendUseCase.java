package com.danphe.rivo.service.internal.otp.usecase;

import com.danphe.rivo.common.library.base.service.api.UseCase;
import com.danphe.rivo.repository.api.ClientOtpRepository;
import com.danphe.rivo.service.api.OtpSender;
import org.springframework.security.crypto.password.PasswordEncoder;

public class OtpResendUseCase implements UseCase<OtpGenerateCommand, String> {

  private final OtpGenerateUseCase generateUseCase;

  public OtpResendUseCase(
      ClientOtpRepository repository,
      PasswordEncoder passwordEncoder,
      OtpSender otpSender) {
    this.generateUseCase = new OtpGenerateUseCase(repository, passwordEncoder, otpSender);
  }

  @Override
  public String execute(OtpGenerateCommand request) {
    validate(request);
    return process(request);
  }

  protected void validate(OtpGenerateCommand request) {
    generateUseCase.validate(request);
  }

  protected String process(OtpGenerateCommand request) {
    return generateUseCase.process(request);
  }
}
