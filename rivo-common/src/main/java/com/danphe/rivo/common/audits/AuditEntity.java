package com.danphe.rivo.common.audits;

import com.danphe.rivo.common.library.base.dao.entity.Entity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuditEntity implements Entity {
  private String id;
  private String entityKey;
  private String requestType;
  private String newEntity;
  private String oldEntity;
  private String requestedBy;
  private LocalDateTime requestedOn;
  private String respondedBy;
  private LocalDateTime respondedOn;
  private String remarks;
  private boolean active;
  private boolean deleted;
  private String entity;
  private String resource;
  private LocalDateTime createdOn;
  private LocalDateTime lastModifiedOn;
  private String createdBy;
  private String lastModifiedBy;
  private LocalDateTime deletedOn;
  private String deletedBy;
  private String remark;
  private Long version;
}
