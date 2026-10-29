package com.danphe.rivo.common.matchrequest;

import com.danphe.rivo.common.library.base.filter.AbstractFilter;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchRequestFilter extends AbstractFilter {

  private String requestedBy;
  private String requestType;
  private String status;
  private String game;
  private String category;
  private Boolean urgent;
  private LocalDateTime timeFrom;
  private LocalDateTime timeTo;

  @Override
  public String getSortColumn() {
    if (sortField == null || sortField.isBlank()) {
      return "created_on";
    }
    return switch (sortField.toLowerCase()) {
      case "game" -> "game";
      case "category" -> "category";
      case "requesttype", "request_type" -> "request_type";
      case "status" -> "status";
      case "time" -> "\"time\"";
      case "urgent" -> "urgent";
      case "last_modified_on", "lastmodifiedon" -> "last_modified_on";
      case "active" -> "active";
      default -> "created_on";
    };
  }
}
