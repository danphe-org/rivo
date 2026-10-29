package com.danphe.rivo.service.internal.masterdata.usecase;

import com.danphe.rivo.common.abstracts.AbstractDeleteUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MasterDataRepository;
import java.time.LocalDateTime;

public class MasterDataDeleteUseCase extends AbstractDeleteUseCase<MasterDataDomain, Void> {

  private final MasterDataRepository repository;
  private final UserDomain actor;

  public MasterDataDeleteUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      MasterDataRepository repository,
      UserDomain actor) {
    super(converter, auditDao, repository);
    this.repository = repository;
    this.actor = actor;
  }

  @Override
  protected void validate(MasterDataDomain req) {
    if (req.getId() == null || req.getId().isBlank()) {
      throw new AppException("Master data id is required for delete");
    }

    repository.findById(req.getId())
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new NotFoundException("Master data not found with id: " + req.getId()));
  }

  @Override
  protected void preProcessing(MasterDataDomain domain, MasterDataDomain oldDomain) {
    if (actor != null) {
      domain.setDeletedBy(actor.getId());
    }
    domain.setDeletedOn(LocalDateTime.now());
    domain.setActive(false);
    domain.setDeleted(true);
  }

  @Override
  protected String getEntity() {
    return SystemModules.MASTER_DATA.getEntity();
  }

  @Override
  protected Void prepareResponse(MasterDataDomain domain) {
    return null;
  }
}
