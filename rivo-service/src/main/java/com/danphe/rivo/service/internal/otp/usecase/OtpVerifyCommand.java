package com.danphe.rivo.service.internal.otp.usecase;

import com.danphe.rivo.common.otp.OtpPurpose;

public record OtpVerifyCommand(
    String identifier,
    String plainOtp,
    OtpPurpose purpose
) {}
