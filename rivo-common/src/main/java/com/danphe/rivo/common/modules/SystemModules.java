package com.danphe.rivo.common.modules;

import lombok.Getter;

@Getter
public enum SystemModules {
  USER("users"),
  USER_ROLE("user_roles"),
  MATCH_REQUEST("match_requests"),
  MASTER_DATA("master_data");

  private final String entity;

  SystemModules(String entity) {
    this.entity = entity;
  }
}
