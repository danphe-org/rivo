package com.danphe.rivo.repository.internal.masterdata;

import com.danphe.rivo.common.abstracts.AbstractEntityConverter;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import org.springframework.stereotype.Component;

@Component
public class MasterDataEntityConverter
    extends AbstractEntityConverter<MasterDataDomain, MasterDataEntity> {

  @Override
  public MasterDataEntity toEntity(MasterDataDomain domain) {
    if (domain == null) return null;
    MasterDataEntity entity = new MasterDataEntity();
    copyAuditFields(domain, entity);
    entity.setTitle(domain.getTitle());
    entity.setCode(domain.getCode());
    return entity;
  }

  @Override
  public MasterDataDomain toDomain(MasterDataEntity entity) {
    if (entity == null) return null;
    MasterDataDomain domain = new MasterDataDomain();
    copyAuditFields(entity, domain);
    domain.setTitle(entity.getTitle());
    domain.setCode(entity.getCode());
    return domain;
  }
}
