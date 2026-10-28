package com.danphe.rivo.common.library.base.service.api;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractDomain implements Domain {
  protected String id;
  protected LocalDateTime createdOn;
  protected String createdBy;
  protected LocalDateTime lastModifiedOn;
  protected String lastModifiedBy;
  protected LocalDateTime deletedOn;
  protected String deletedBy;
  protected boolean active = true;
  protected boolean deleted = false;
  protected String remark;
  protected Long version = 1L;
}
