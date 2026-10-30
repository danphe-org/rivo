package com.danphe.rivo.repository.internal.systemuser;

import com.danphe.rivo.common.abstracts.AbstractEntityConverter;
import com.danphe.rivo.common.user.UserDomain;
import org.springframework.stereotype.Component;

@Component
public class SystemUserEntityConverter extends AbstractEntityConverter<UserDomain, SystemUserEntity> {

  @Override
  public SystemUserEntity toEntity(UserDomain domain) {
    if (domain == null) return null;
    SystemUserEntity entity = new SystemUserEntity();
    copyAuditFields(domain, entity);
    entity.setName(domain.getName());
    entity.setEmail(domain.getEmail());
    entity.setPassword(domain.getPassword());
    entity.setPhone(domain.getPhone());
    return entity;
  }

  @Override
  public UserDomain toDomain(SystemUserEntity entity) {
    if (entity == null) return null;
    UserDomain domain = new UserDomain();
    copyAuditFields(entity, domain);
    domain.setName(entity.getName());
    domain.setEmail(entity.getEmail());
    domain.setPassword(entity.getPassword());
    domain.setPhone(entity.getPhone());
    return domain;
  }
}
