package com.danphe.rivo.common.matchrequest;

public enum MatchRequestType {
  OPPONENT,
  TEAMMATE;

  public static MatchRequestType fromString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String normalized = value.trim().toUpperCase();
    for (MatchRequestType type : values()) {
      if (type.name().equals(normalized)) {
        return type;
      }
    }
    return null;
  }
}
