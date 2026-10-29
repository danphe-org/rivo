package com.danphe.rivo.common.abstracts;

import com.danphe.rivo.common.library.base.dao.entity.AbstractEntity;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.service.api.AbstractDomain;
import java.util.Collections;
import java.util.List;

public abstract class AbstractEntityConverter<D extends AbstractDomain, E extends AbstractEntity>
    implements EntityConverter<D, E> {

  public void copyAuditFields(D domain, E entity) {
    copyAuditFieldsToEntity(domain, entity);
  }

  public void copyAuditFields(E entity, D domain) {
    copyAuditFieldsToDomain(entity, domain);
  }

  public void copyAuditFieldsToEntity(D domain, E entity) {
    if (domain == null || entity == null) {
      return;
    }
    entity.setId(domain.getId());
    entity.setCreatedOn(domain.getCreatedOn());
    entity.setCreatedBy(domain.getCreatedBy());
    entity.setLastModifiedOn(domain.getLastModifiedOn());
    entity.setLastModifiedBy(domain.getLastModifiedBy());
    entity.setDeletedOn(domain.getDeletedOn());
    entity.setDeletedBy(domain.getDeletedBy());
    entity.setActive(domain.isActive());
    entity.setDeleted(domain.isDeleted());
    entity.setRemark(domain.getRemark());
    entity.setVersion(domain.getVersion());
  }

  public void copyAuditFieldsToDomain(E entity, D domain) {
    if (entity == null || domain == null) {
      return;
    }
    domain.setId(entity.getId());
    domain.setCreatedOn(entity.getCreatedOn());
    domain.setCreatedBy(entity.getCreatedBy());
    domain.setLastModifiedOn(entity.getLastModifiedOn());
    domain.setLastModifiedBy(entity.getLastModifiedBy());
    domain.setDeletedOn(entity.getDeletedOn());
    domain.setDeletedBy(entity.getDeletedBy());
    domain.setActive(entity.isActive());
    domain.setDeleted(entity.isDeleted());
    domain.setRemark(entity.getRemark());
    domain.setVersion(entity.getVersion());
  }

  @Override
  public List<D> toDomainList(List<E> entities) {
    if (entities == null) {
      return Collections.emptyList();
    }
    return entities.stream().map(this::toDomain).toList();
  }

  public List<E> toEntityList(List<D> domains) {
    if (domains == null) {
      return Collections.emptyList();
    }
    return domains.stream().map(this::toEntity).toList();
  }
}
