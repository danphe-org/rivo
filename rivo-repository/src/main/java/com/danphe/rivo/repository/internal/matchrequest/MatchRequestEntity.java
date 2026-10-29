package com.danphe.rivo.repository.internal.matchrequest;

import com.danphe.rivo.common.library.base.dao.entity.AbstractEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MatchRequestEntity extends AbstractEntity {

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
}
