package com.danphe.rivo.repository.internal.userrole;

import com.danphe.rivo.common.abstracts.AbstractEntityConverter;
import com.danphe.rivo.common.user.UserRoleDomain;
import org.springframework.stereotype.Component;

@Component
public class UserRoleEntityConverter
    extends AbstractEntityConverter<UserRoleDomain, UserRoleEntity> {

  @Override
  public UserRoleEntity toEntity(UserRoleDomain domain) {
    if (domain == null) return null;
    UserRoleEntity entity = new UserRoleEntity();
    copyAuditFields(domain, entity);
    entity.setUserId(domain.getUserId());
    entity.setRole(domain.getRole());
    return entity;
  }

  @Override
  public UserRoleDomain toDomain(UserRoleEntity entity) {
    if (entity == null) return null;
    UserRoleDomain domain = new UserRoleDomain();
    copyAuditFields(entity, domain);
    domain.setUserId(entity.getUserId());
    domain.setRole(entity.getRole());
    return domain;
  }
}
