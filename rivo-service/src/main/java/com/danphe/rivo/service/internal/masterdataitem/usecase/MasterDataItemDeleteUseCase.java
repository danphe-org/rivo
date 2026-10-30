package com.danphe.rivo.service.internal.masterdataitem.usecase;

import com.danphe.rivo.common.abstracts.AbstractDeleteUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MasterDataItemRepository;
import java.time.LocalDateTime;

public class MasterDataItemDeleteUseCase
    extends AbstractDeleteUseCase<MasterDataItemDomain, Void> {

  private final MasterDataItemRepository repository;
  private final UserDomain actor;

  public MasterDataItemDeleteUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      MasterDataItemRepository repository,
      UserDomain actor) {
    super(converter, auditDao, repository);
    this.repository = repository;
    this.actor = actor;
  }

  @Override
  protected void validate(MasterDataItemDomain req) {
    if (req.getId() == null || req.getId().isBlank()) {
      throw new AppException("Master data item id is required for delete");
    }

    repository.findById(req.getId())
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new NotFoundException("Master data item not found with id: " + req.getId()));
  }

  @Override
  protected void preProcessing(MasterDataItemDomain domain, MasterDataItemDomain oldDomain) {
    if (actor != null) {
      domain.setDeletedBy(actor.getId());
    }
    domain.setDeletedOn(LocalDateTime.now());
    domain.setActive(false);
    domain.setDeleted(true);
  }

  @Override
  protected String getEntity() {
    return SystemModules.MASTER_DATA_ITEM.getEntity();
  }

  @Override
  protected Void prepareResponse(MasterDataItemDomain domain) {
    return null;
  }
}
