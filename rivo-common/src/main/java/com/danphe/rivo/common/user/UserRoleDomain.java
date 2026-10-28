package com.danphe.rivo.common.user;

import com.danphe.rivo.common.library.base.service.api.AbstractDomain;
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
public class UserRoleDomain extends AbstractDomain {
  private String userId;
  private String role;

  public UserRole getUserRole() {
    return UserRole.fromString(this.role);
  }

  public void setUserRole(UserRole userRole) {
    this.role = userRole != null ? userRole.name() : UserRole.CLIENT.name();
  }
}
