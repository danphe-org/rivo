package com.danphe.rivo.repository.internal.clientuser;

import com.danphe.rivo.common.abstracts.AbstractEntityConverter;
import com.danphe.rivo.common.user.UserDomain;
import org.springframework.stereotype.Component;

@Component
public class ClientUserEntityConverter extends AbstractEntityConverter<UserDomain, ClientUserEntity> {

  @Override
  public ClientUserEntity toEntity(UserDomain domain) {
    if (domain == null) return null;
    ClientUserEntity entity = new ClientUserEntity();
    copyAuditFields(domain, entity);
    entity.setName(domain.getName());
    entity.setEmail(domain.getEmail());
    entity.setPassword(domain.getPassword());
    entity.setPhone(domain.getPhone());
    return entity;
  }

  @Override
  public UserDomain toDomain(ClientUserEntity entity) {
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
