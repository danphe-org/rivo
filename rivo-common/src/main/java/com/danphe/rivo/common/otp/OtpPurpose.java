package com.danphe.rivo.common.otp;

public enum OtpPurpose {
  REGISTRATION,
  PASSWORD_RESET,
  LOGIN;

  public static OtpPurpose fromString(String value) {
    if (value == null || value.isBlank()) {
      return REGISTRATION;
    }
    for (OtpPurpose purpose : values()) {
      if (purpose.name().equalsIgnoreCase(value.trim())) {
        return purpose;
      }
    }
    return REGISTRATION;
  }
}
