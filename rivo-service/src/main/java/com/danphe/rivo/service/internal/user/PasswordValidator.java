package com.danphe.rivo.service.internal.user;

import com.danphe.rivo.common.library.base.exception.AppException;
import java.util.regex.Pattern;

public class PasswordValidator {

  private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
  private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
  private static final Pattern HAS_DIGIT = Pattern.compile("\\d");
  private static final Pattern HAS_SPECIAL = Pattern.compile("[^a-zA-Z0-9]");

  public static void validate(String password) {
    if (password == null || password.isBlank()) {
      throw new AppException("Password cannot be empty");
    }
    if (password.length() < 8) {
      throw new AppException("Password must be at least 8 characters long");
    }
    if (password.length() > 128) {
      throw new AppException("Password must not exceed 128 characters");
    }
    if (password.contains(" ")) {
      throw new AppException("Password must not contain spaces");
    }
    if (!HAS_UPPERCASE.matcher(password).find()) {
      throw new AppException("Password must contain at least one uppercase letter");
    }
    if (!HAS_LOWERCASE.matcher(password).find()) {
      throw new AppException("Password must contain at least one lowercase letter");
    }
    if (!HAS_DIGIT.matcher(password).find()) {
      throw new AppException("Password must contain at least one number");
    }
    if (!HAS_SPECIAL.matcher(password).find()) {
      throw new AppException("Password must contain at least one special character");
    }
  }
}
