package com.danphe.rivo.common.matchrequest;

public enum MatchRequestStatus {
  OPEN,
  MATCHED,
  FILLED,
  CANCELLED,
  COMPLETED;

  public static MatchRequestStatus fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String normalized = value.trim().toUpperCase();
    for (MatchRequestStatus status : values()) {
      if (status.name().equals(normalized)) {
        return status;
      }
    }
    return null;
  }
}
