package com.danphe.rivo.common.masterdataitem;

import com.danphe.rivo.common.library.base.filter.AbstractFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasterDataItemFilter extends AbstractFilter {

  private String masterDataId;
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
      case "master_data_id", "masterdataid" -> "master_data_id";
      case "last_modified_on", "lastmodifiedon" -> "last_modified_on";
      case "active" -> "active";
      default -> "created_on";
    };
  }
}
