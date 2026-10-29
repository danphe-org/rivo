package com.danphe.rivo.common.matchrequest;

import com.danphe.rivo.common.library.base.service.api.AbstractDomain;
import java.time.LocalDateTime;
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
public class MatchRequestDomain extends AbstractDomain {

  private String requestedBy;
  private String requestType;
  private String status;
  private String game;
  private String category;
  private String message;
  private LocalDateTime time;
  private boolean urgent;

  private String requestedByName;
  private String requestedByEmail;

  public MatchRequestType getMatchRequestType() {
    return MatchRequestType.fromString(this.requestType);
  }

  public MatchRequestStatus getMatchRequestStatus() {
    return MatchRequestStatus.fromString(this.status);
  }
}
