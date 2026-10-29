package com.danphe.rivo.repository.internal.user;

import com.danphe.rivo.common.abstracts.AbstractEntityConverter;
import com.danphe.rivo.common.user.UserDomain;
import org.springframework.stereotype.Component;

@Component
public class UserEntityConverter extends AbstractEntityConverter<UserDomain, UserEntity> {

  @Override
  public UserEntity toEntity(UserDomain domain) {
    if (domain == null) return null;
    UserEntity entity = new UserEntity();
    copyAuditFields(domain, entity);
    entity.setName(domain.getName());
    entity.setEmail(domain.getEmail());
    entity.setPassword(domain.getPassword());
    entity.setPhone(domain.getPhone());
    return entity;
  }

  @Override
  public UserDomain toDomain(UserEntity entity) {
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
