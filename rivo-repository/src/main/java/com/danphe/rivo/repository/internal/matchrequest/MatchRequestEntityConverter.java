package com.danphe.rivo.repository.internal.matchrequest;

import com.danphe.rivo.common.abstracts.AbstractEntityConverter;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import org.springframework.stereotype.Component;

@Component
public class MatchRequestEntityConverter
    extends AbstractEntityConverter<MatchRequestDomain, MatchRequestEntity> {

  @Override
  public MatchRequestEntity toEntity(MatchRequestDomain domain) {
    if (domain == null) return null;
    MatchRequestEntity entity = new MatchRequestEntity();
    copyAuditFields(domain, entity);
    entity.setRequestedBy(domain.getRequestedBy());
    entity.setRequestType(domain.getRequestType());
    entity.setStatus(domain.getStatus());
    entity.setGame(domain.getGame());
    entity.setCategory(domain.getCategory());
    entity.setMessage(domain.getMessage());
    entity.setTime(domain.getTime());
    entity.setUrgent(domain.isUrgent());
    entity.setRequestedByName(domain.getRequestedByName());
    entity.setRequestedByEmail(domain.getRequestedByEmail());
    return entity;
  }

  @Override
  public MatchRequestDomain toDomain(MatchRequestEntity entity) {
    if (entity == null) return null;
    MatchRequestDomain domain = new MatchRequestDomain();
    copyAuditFields(entity, domain);
    domain.setRequestedBy(entity.getRequestedBy());
    domain.setRequestType(entity.getRequestType());
    domain.setStatus(entity.getStatus());
    domain.setGame(entity.getGame());
    domain.setCategory(entity.getCategory());
    domain.setMessage(entity.getMessage());
    domain.setTime(entity.getTime());
    domain.setUrgent(entity.isUrgent());
    domain.setRequestedByName(entity.getRequestedByName());
    domain.setRequestedByEmail(entity.getRequestedByEmail());
    return domain;
  }
}
