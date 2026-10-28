package com.danphe.rivo.common.library.base.modal;

import java.time.LocalDateTime;

public interface AuditableModal {

  String getId();

  void setId(String id);

  LocalDateTime getCreatedOn();

  void setCreatedOn(LocalDateTime createdOn);

  LocalDateTime getLastModifiedOn();

  void setLastModifiedOn(LocalDateTime lastModifiedOn);

  String getCreatedBy();

  void setCreatedBy(String createdBy);

  String getLastModifiedBy();

  void setLastModifiedBy(String lastModifiedBy);

  LocalDateTime getDeletedOn();

  void setDeletedOn(LocalDateTime deletedOn);

  String getDeletedBy();

  void setDeletedBy(String deletedBy);

  boolean isActive();

  void setActive(boolean active);

  boolean isDeleted();

  void setDeleted(boolean deleted);

  String getRemark();

  void setRemark(String remark);

  Long getVersion();

  void setVersion(Long version);
}
