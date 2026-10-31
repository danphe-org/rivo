package com.danphe.rivo.service.api;

import com.danphe.rivo.common.otp.OtpPurpose;

public interface OtpService {

  void sendOtp(String identifier, OtpPurpose purpose);

  boolean verifyOtp(String identifier, String plainOtp, OtpPurpose purpose);

  void resendOtp(String identifier, OtpPurpose purpose);
}
