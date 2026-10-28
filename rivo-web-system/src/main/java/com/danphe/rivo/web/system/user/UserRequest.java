package com.danphe.rivo.web.system.user;

import com.danphe.rivo.common.user.UserDomain;

public record UserRequest(
    String name,
    String email,
    String password,
    String phone,
    String role,
    Boolean active
) {
  public UserDomain toDomain() {
    UserDomain domain = new UserDomain();
    domain.setName(name);
    domain.setEmail(email);
    domain.setPassword(password);
    domain.setPhone(phone);
    if (role != null && !role.isBlank()) {
      domain.setRole(role);
    }
    if (active != null) {
      domain.setActive(active);
    }
    return domain;
  }
}
