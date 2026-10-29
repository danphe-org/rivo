package com.danphe.rivo.common.masterdata;

import com.danphe.rivo.common.library.base.filter.AbstractFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasterDataFilter extends AbstractFilter {

  private String title;
  private String code;

  @Override
  public String getSortColumn() {
    if (sortField == null || sortField.isBlank()) {
      return "created_on";
    }
    return switch (sortField.toLowerCase()) {
      case "title" -> "title";
      case "code" -> "code";
      case "last_modified_on", "lastmodifiedon" -> "last_modified_on";
      case "active" -> "active";
      default -> "created_on";
    };
  }
}
