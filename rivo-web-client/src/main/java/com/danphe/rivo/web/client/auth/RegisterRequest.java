package com.danphe.rivo.web.client.auth;

import com.danphe.rivo.common.user.UserDomain;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "Name is required")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
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
