package com.danphe.rivo.common.user;

import com.danphe.rivo.common.library.base.filter.AbstractFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFilter extends AbstractFilter {
  private String name;
  private String email;
  private String role;
  private String phone;
  private Boolean verified;

  @Override
  public String getSortColumn() {
    if (sortField == null || sortField.isBlank()) {
      return "created_on";
    }
    return switch (sortField.toLowerCase()) {
      case "name" -> "name";
      case "email" -> "email";
      case "phone" -> "phone";
      case "last_modified_on", "lastmodifiedon" -> "last_modified_on";
      case "active" -> "active";
      default -> "created_on";
    };
  }
}
