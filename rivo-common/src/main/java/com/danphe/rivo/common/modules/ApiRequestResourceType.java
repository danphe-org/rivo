package com.danphe.rivo.common.modules;

import lombok.Getter;

@Getter
public enum ApiRequestResourceType {
  WEB_SYSTEM("WEB_SYSTEM"),
  WEB_CLIENT("WEB_CLIENT");

  private final String value;

  ApiRequestResourceType(String value) {
    this.value = value;
  }
}
