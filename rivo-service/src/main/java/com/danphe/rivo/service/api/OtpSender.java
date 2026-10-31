package com.danphe.rivo.service.api;

import com.danphe.rivo.common.otp.OtpPurpose;

public interface OtpSender {

  void sendOtp(String identifier, String plainOtp, OtpPurpose purpose);
}
