package com.danphe.rivo.repository.internal.auditlog;

import com.danphe.rivo.common.audits.AuditDomain;
import com.danphe.rivo.common.audits.AuditEntity;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AuditLogConvertor implements EntityConverter<AuditDomain, AuditEntity> {

  @Override
  public AuditEntity toEntity(AuditDomain domain) {
    if (domain == null) return null;
    AuditEntity entity = new AuditEntity();
    entity.setId(domain.getId());
    entity.setEntityKey(domain.getEntityKey());
    entity.setRequestType(domain.getRequestType());
    entity.setNewEntity(domain.getNewEntity());
    entity.setOldEntity(domain.getOldEntity());
    entity.setRequestedBy(domain.getRequestedBy());
    entity.setRequestedOn(domain.getRequestedOn());
    entity.setRespondedBy(domain.getRespondedBy());
    entity.setRespondedOn(domain.getRespondedOn());
    entity.setRemarks(domain.getRemarks());
    entity.setEntity(domain.getEntity());
    entity.setResource(domain.getResource());
    entity.setActive(domain.isActive());
    entity.setDeleted(domain.isDeleted());
    entity.setRemark(domain.getRemark());
    entity.setVersion(domain.getVersion());
    return entity;
  }

  @Override
  public AuditDomain toDomain(AuditEntity entity) {
    if (entity == null) return null;
    AuditDomain domain = new AuditDomain();
    domain.setId(entity.getId());
    domain.setEntityKey(entity.getEntityKey());
    domain.setRequestType(entity.getRequestType());
    domain.setNewEntity(entity.getNewEntity());
    domain.setOldEntity(entity.getOldEntity());
    domain.setRequestedBy(entity.getRequestedBy());
    domain.setRequestedOn(entity.getRequestedOn());
    domain.setRespondedBy(entity.getRespondedBy());
    domain.setRespondedOn(entity.getRespondedOn());
    domain.setRemarks(entity.getRemarks());
    domain.setEntity(entity.getEntity());
    domain.setResource(entity.getResource());
    domain.setActive(entity.isActive());
    domain.setDeleted(entity.isDeleted());
    domain.setRemark(entity.getRemark());
    domain.setVersion(entity.getVersion());
    return domain;
  }

  @Override
  public List<AuditDomain> toDomainList(List<AuditEntity> entities) {
    if (entities == null) return Collections.emptyList();
    return entities.stream().map(this::toDomain).toList();
  }
}
