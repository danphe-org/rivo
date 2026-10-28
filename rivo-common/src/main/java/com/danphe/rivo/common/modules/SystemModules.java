package com.danphe.rivo.common.modules;

import lombok.Getter;

@Getter
public enum SystemModules {
  USER("users"),
  USER_ROLE("user_roles");

  private final String entity;

  SystemModules(String entity) {
    this.entity = entity;
  }
}
