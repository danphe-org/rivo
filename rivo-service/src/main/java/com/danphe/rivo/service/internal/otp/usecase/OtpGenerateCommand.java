package com.danphe.rivo.service.internal.otp.usecase;

import com.danphe.rivo.common.otp.OtpPurpose;

public record OtpGenerateCommand(
    String identifier,
    OtpPurpose purpose
) {}
