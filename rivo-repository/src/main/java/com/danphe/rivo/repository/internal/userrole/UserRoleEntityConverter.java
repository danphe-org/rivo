package com.danphe.rivo.repository.internal.userrole;

import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.user.UserRoleDomain;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserRoleEntityConverter implements EntityConverter<UserRoleDomain, UserRoleEntity> {

  @Override
  public UserRoleEntity toEntity(UserRoleDomain domain) {
    if (domain == null) return null;
    UserRoleEntity entity = new UserRoleEntity();
    entity.setId(domain.getId());
    entity.setUserId(domain.getUserId());
    entity.setRole(domain.getRole());
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
    return entity;
  }

  @Override
  public UserRoleDomain toDomain(UserRoleEntity entity) {
    if (entity == null) return null;
    UserRoleDomain domain = new UserRoleDomain();
    domain.setId(entity.getId());
    domain.setUserId(entity.getUserId());
    domain.setRole(entity.getRole());
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
    return domain;
  }

  @Override
  public List<UserRoleDomain> toDomainList(List<UserRoleEntity> entities) {
    if (entities == null) return Collections.emptyList();
    return entities.stream().map(this::toDomain).toList();
  }
}
