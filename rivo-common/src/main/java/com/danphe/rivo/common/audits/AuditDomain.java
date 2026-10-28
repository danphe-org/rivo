package com.danphe.rivo.common.audits;

import com.danphe.rivo.common.library.base.service.api.AbstractDomain;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuditDomain extends AbstractDomain {
  private String entityKey;
  private String requestType;
  private String newEntity;
  private String oldEntity;
  private String requestedBy;
  private LocalDateTime requestedOn;
  private String respondedBy;
  private LocalDateTime respondedOn;
  private String remarks;
  private String entity;
  private String resource;
}
