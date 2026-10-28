package com.danphe.rivo.common.user;

import com.danphe.rivo.common.library.base.service.api.AbstractDomain;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDomain extends AbstractDomain {

  private String name;
  private String email;

  @JsonProperty(access = Access.WRITE_ONLY)
  private String password;

  private String phone;

  @Builder.Default
  private List<UserRoleDomain> roles = new ArrayList<>();

  private String role;

  public String getRole() {
    if (this.roles != null && !this.roles.isEmpty()) {
      for (UserRoleDomain r : this.roles) {
        if (!r.isDeleted() && r.isActive() && r.getRole() != null) {
          return r.getRole();
        }
      }
      return this.roles.get(0).getRole();
    }
    return this.role != null ? this.role : UserRole.CLIENT.name();
  }

  public UserRole getUserRole() {
    return UserRole.fromString(getRole());
  }

  public void setUserRole(UserRole userRole) {
    setRole(userRole != null ? userRole.name() : UserRole.CLIENT.name());
  }

  public boolean isSystemUser() {
    return getUserRole() == UserRole.SYSTEM_USER;
  }

  public boolean isClient() {
    return getUserRole() == UserRole.CLIENT;
  }

  public void setRole(String newRole) {
    this.role = newRole;
    if (newRole != null && !newRole.isBlank()) {
      String upper = newRole.trim().toUpperCase();
      if (this.roles == null) {
        this.roles = new ArrayList<>();
      }
      if (!this.roles.isEmpty()) {
        for (UserRoleDomain r : this.roles) {
          if (!r.isDeleted() && r.isActive()) {
            r.setRole(upper);
            return;
          }
        }
        this.roles.get(0).setRole(upper);
      } else {
        UserRoleDomain ur = new UserRoleDomain();
        ur.setUserId(this.getId());
        ur.setRole(upper);
        ur.setActive(true);
        ur.setDeleted(false);
        this.roles.add(ur);
      }
    }
  }

  public boolean isAdmin() {
    return isSystemUser() || hasRole("ADMIN", "STAFF", "SYSTEM_USER", "SYSTEM");
  }

  public boolean hasRole(String... targetRoles) {
    if (targetRoles == null || targetRoles.length == 0) {
      return false;
    }
    Set<String> targets = Arrays.stream(targetRoles)
        .map(String::trim)
        .map(String::toUpperCase)
        .collect(Collectors.toSet());

    if (this.roles != null) {
      for (UserRoleDomain r : this.roles) {
        if (!r.isDeleted() && r.isActive() && r.getRole() != null) {
          if (targets.contains(r.getRole().trim().toUpperCase())) {
            return true;
          }
        }
      }
    }
    if (this.role != null && targets.contains(this.role.trim().toUpperCase())) {
      return true;
    }
    return false;
  }
}
