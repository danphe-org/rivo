package com.danphe.rivo.repository.internal.masterdataitem;

import com.danphe.rivo.common.abstracts.AbstractEntityConverter;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import org.springframework.stereotype.Component;

@Component
public class MasterDataItemEntityConverter
    extends AbstractEntityConverter<MasterDataItemDomain, MasterDataItemEntity> {

  @Override
  public MasterDataItemEntity toEntity(MasterDataItemDomain domain) {
    if (domain == null) return null;
    MasterDataItemEntity entity = new MasterDataItemEntity();
    copyAuditFields(domain, entity);
    entity.setMasterDataId(domain.getMasterDataId());
    entity.setTitle(domain.getTitle());
    entity.setCode(domain.getCode());
    entity.setDescription(domain.getDescription());
    return entity;
  }

  @Override
  public MasterDataItemDomain toDomain(MasterDataItemEntity entity) {
    if (entity == null) return null;
    MasterDataItemDomain domain = new MasterDataItemDomain();
    copyAuditFields(entity, domain);
    domain.setMasterDataId(entity.getMasterDataId());
    domain.setTitle(entity.getTitle());
    domain.setCode(entity.getCode());
    domain.setDescription(entity.getDescription());
    return domain;
  }
}
