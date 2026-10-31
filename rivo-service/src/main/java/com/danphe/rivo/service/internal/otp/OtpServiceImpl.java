package com.danphe.rivo.service.internal.otp;

import com.danphe.rivo.common.otp.OtpPurpose;
import com.danphe.rivo.repository.api.ClientOtpRepository;
import com.danphe.rivo.service.api.OtpSender;
import com.danphe.rivo.service.api.OtpService;
import com.danphe.rivo.service.internal.otp.usecase.OtpGenerateCommand;
import com.danphe.rivo.service.internal.otp.usecase.OtpGenerateUseCase;
import com.danphe.rivo.service.internal.otp.usecase.OtpResendUseCase;
import com.danphe.rivo.service.internal.otp.usecase.OtpVerifyCommand;
import com.danphe.rivo.service.internal.otp.usecase.OtpVerifyUseCase;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("otpService")
public class OtpServiceImpl implements OtpService {

  private final ClientOtpRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final OtpSender otpSender;

  public OtpServiceImpl(
      ClientOtpRepository repository,
      PasswordEncoder passwordEncoder,
      OtpSender otpSender) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
    this.otpSender = otpSender;
  }

  @Override
  public void sendOtp(String identifier, OtpPurpose purpose) {
    OtpGenerateUseCase useCase = new OtpGenerateUseCase(repository, passwordEncoder, otpSender);
    useCase.execute(new OtpGenerateCommand(identifier, purpose));
  }

  @Override
  public boolean verifyOtp(String identifier, String plainOtp, OtpPurpose purpose) {
    OtpVerifyUseCase useCase = new OtpVerifyUseCase(repository, passwordEncoder);
    return useCase.execute(new OtpVerifyCommand(identifier, plainOtp, purpose));
  }

  @Override
  public void resendOtp(String identifier, OtpPurpose purpose) {
    OtpResendUseCase useCase = new OtpResendUseCase(repository, passwordEncoder, otpSender);
    useCase.execute(new OtpGenerateCommand(identifier, purpose));
  }
}
