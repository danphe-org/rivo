package com.danphe.rivo.repository.internal.otp;

import com.danphe.rivo.common.abstracts.AbstractEntityConverter;
import com.danphe.rivo.common.otp.OtpDomain;
import org.springframework.stereotype.Component;

@Component
public class ClientOtpEntityConverter extends AbstractEntityConverter<OtpDomain, ClientOtpEntity> {

  @Override
  public ClientOtpEntity toEntity(OtpDomain domain) {
    if (domain == null) return null;
    ClientOtpEntity entity = new ClientOtpEntity();
    copyAuditFields(domain, entity);
    entity.setIdentifier(domain.getIdentifier());
    entity.setOtpHash(domain.getOtpHash());
    entity.setPurpose(domain.getPurpose());
    entity.setAttempts(domain.getAttempts());
    entity.setMaxAttempts(domain.getMaxAttempts());
    entity.setResendAvailableAt(domain.getResendAvailableAt());
    entity.setExpiresAt(domain.getExpiresAt());
    entity.setConsumed(domain.isConsumed());
    entity.setConsumedAt(domain.getConsumedAt());
    return entity;
  }

  @Override
  public OtpDomain toDomain(ClientOtpEntity entity) {
    if (entity == null) return null;
    OtpDomain domain = new OtpDomain();
    copyAuditFields(entity, domain);
    domain.setIdentifier(entity.getIdentifier());
    domain.setOtpHash(entity.getOtpHash());
    domain.setPurpose(entity.getPurpose());
    domain.setAttempts(entity.getAttempts());
    domain.setMaxAttempts(entity.getMaxAttempts());
    domain.setResendAvailableAt(entity.getResendAvailableAt());
    domain.setExpiresAt(entity.getExpiresAt());
    domain.setConsumed(entity.isConsumed());
    domain.setConsumedAt(entity.getConsumedAt());
    return domain;
  }
}
