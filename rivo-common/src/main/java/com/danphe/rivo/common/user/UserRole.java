package com.danphe.rivo.common.user;

public enum UserRole {
  SYSTEM_USER,
  CLIENT;

  public boolean isSystemUser() {
    return this == SYSTEM_USER;
  }

  public boolean isClient() {
    return this == CLIENT;
  }

  public static UserRole fromString(String role) {
    if (role == null || role.isBlank()) {
      return CLIENT;
    }
    String trimmed = role.trim().toUpperCase();
    if ("SYSTEM_USER".equals(trimmed) || "SYSTEM".equals(trimmed) || "ADMIN".equals(trimmed) || "STAFF".equals(trimmed)) {
      return SYSTEM_USER;
    }
    return CLIENT;
  }
}
