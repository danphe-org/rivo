package com.danphe.rivo.web.system.user;

import com.danphe.rivo.common.user.UserDomain;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
    String id,
    String name,
    String email,
    String phone,
    String role,
    List<UserRoleResponse> roles,
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("refresh_token")
    String refreshToken,
    @JsonProperty("token_type")
    String tokenType,
    @JsonProperty("created_on")
    LocalDateTime createdOn,
    @JsonProperty("modified_on")
    LocalDateTime modifiedOn,
    @JsonProperty("created_by")
    String createdBy,
    @JsonProperty("modified_by")
    String modifiedBy,
    boolean active,
    boolean deleted
) {
  public static UserResponse from(UserDomain domain) {
    if (domain == null) return null;
    List<UserRoleResponse> roleResponses = domain.getRoles() != null
        ? domain.getRoles().stream().map(UserRoleResponse::from).toList()
        : Collections.emptyList();

    return new UserResponse(
        domain.getId(),
        domain.getName(),
        domain.getEmail(),
        domain.getPhone(),
        domain.getRole(),
        roleResponses,
        null,
        null,
        null,
        domain.getCreatedOn(),
        domain.getLastModifiedOn(),
        domain.getCreatedBy(),
        domain.getLastModifiedBy(),
        domain.isActive(),
        domain.isDeleted()
    );
  }

  public static UserResponse withTokens(UserDomain domain, String accessToken, String refreshToken) {
    if (domain == null) return null;
    List<UserRoleResponse> roleResponses = domain.getRoles() != null
        ? domain.getRoles().stream().map(UserRoleResponse::from).toList()
        : Collections.emptyList();

    return new UserResponse(
        domain.getId(),
        domain.getName(),
        domain.getEmail(),
        domain.getPhone(),
        domain.getRole(),
        roleResponses,
        accessToken,
        refreshToken,
        "Bearer",
        domain.getCreatedOn(),
        domain.getLastModifiedOn(),
        domain.getCreatedBy(),
        domain.getLastModifiedBy(),
        domain.isActive(),
        domain.isDeleted()
    );
  }
}
