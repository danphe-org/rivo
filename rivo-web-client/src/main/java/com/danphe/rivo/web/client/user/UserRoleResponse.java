package com.danphe.rivo.web.client.user;

import com.danphe.rivo.common.user.UserRoleDomain;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record UserRoleResponse(
    String id,
    @JsonProperty("user_id")
    String userId,
    String role,
    @JsonProperty("created_on")
    LocalDateTime createdOn,
    @JsonProperty("modified_on")
    LocalDateTime modifiedOn,
    boolean active,
    boolean deleted
) {
  public static UserRoleResponse from(UserRoleDomain domain) {
    if (domain == null) return null;
    return new UserRoleResponse(
        domain.getId(),
        domain.getUserId(),
        domain.getRole(),
        domain.getCreatedOn(),
        domain.getLastModifiedOn(),
        domain.isActive(),
        domain.isDeleted()
    );
  }
}
