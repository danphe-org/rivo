package com.danphe.rivo.common.matchrequest;

import com.danphe.rivo.common.library.base.filter.AbstractFilter;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchRequestFilter extends AbstractFilter {

  @JsonAlias({"requested_by", "requestedBy"})
  private String requestedBy;

  @JsonAlias({"request_type", "requestType"})
  private String requestType;

  private String status;
  private String game;
  private String category;
  private String location;
  private Boolean urgent;

  @JsonAlias({"time_from", "timeFrom"})
  private LocalDateTime timeFrom;

  @JsonAlias({"time_to", "timeTo"})
  private LocalDateTime timeTo;

  @JsonAlias({"upcoming_only", "upcomingOnly"})
  private Boolean upcomingOnly;

  @JsonAlias({"past_only", "pastOnly"})
  private Boolean pastOnly;

  private String viewerId;
  private Boolean admin;
  private LocalDateTime now;

  @Override
  public String getSortColumn() {
    if (sortField == null || sortField.isBlank()) {
      return "created_on";
    }
    return switch (sortField.toLowerCase()) {
      case "game" -> "game";
      case "category" -> "category";
      case "location" -> "location";
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
