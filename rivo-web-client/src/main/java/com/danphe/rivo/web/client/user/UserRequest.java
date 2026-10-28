package com.danphe.rivo.web.client.user;

import com.danphe.rivo.common.user.UserDomain;

public record UserRequest(
    String name,
    String email,
    String password,
    String phone
) {
  public UserDomain toDomain() {
    UserDomain domain = new UserDomain();
    domain.setName(name);
    domain.setEmail(email);
    domain.setPassword(password);
    domain.setPhone(phone);
    return domain;
  }
}
