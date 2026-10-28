package com.danphe.rivo.common.library.base.dao.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractEntity implements Entity {
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
