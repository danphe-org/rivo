package com.danphe.rivo.repository.internal.user;

import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.user.UserDomain;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserEntityConverter implements EntityConverter<UserDomain, UserEntity> {

  @Override
  public UserEntity toEntity(UserDomain domain) {
    if (domain == null) return null;
    UserEntity entity = new UserEntity();
    entity.setId(domain.getId());
    entity.setName(domain.getName());
    entity.setEmail(domain.getEmail());
    entity.setPassword(domain.getPassword());
    entity.setPhone(domain.getPhone());
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
  public UserDomain toDomain(UserEntity entity) {
    if (entity == null) return null;
    UserDomain domain = new UserDomain();
    domain.setId(entity.getId());
    domain.setName(entity.getName());
    domain.setEmail(entity.getEmail());
    domain.setPassword(entity.getPassword());
    domain.setPhone(entity.getPhone());
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
  public List<UserDomain> toDomainList(List<UserEntity> entities) {
    if (entities == null) return Collections.emptyList();
    return entities.stream().map(this::toDomain).toList();
  }
}
