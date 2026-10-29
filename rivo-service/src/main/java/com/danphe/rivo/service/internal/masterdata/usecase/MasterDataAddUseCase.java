package com.danphe.rivo.service.internal.masterdata.usecase;

import com.danphe.rivo.common.abstracts.AbstractAddUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MasterDataRepository;

public class MasterDataAddUseCase
    extends AbstractAddUseCase<MasterDataDomain, MasterDataDomain> {

  private final MasterDataRepository repository;
  private final UserDomain actor;

  public MasterDataAddUseCase(
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
    String normalizedCode = req.getCode() != null ? req.getCode().trim().toUpperCase() : "";
    String normalizedTitle = req.getTitle() != null ? req.getTitle().trim() : "";
    if (repository.findByCodeAndTitle(normalizedCode, normalizedTitle).isPresent()) {
      throw new AppException("Master data with title '" + normalizedTitle + "' already exists under code '" + normalizedCode + "'");
    }
  }

  @Override
  protected void preProcessing(MasterDataDomain domain) {
    domain.setTitle(domain.getTitle().trim());
    domain.setCode(domain.getCode().trim().toUpperCase());
    if (actor != null) {
      domain.setCreatedBy(actor.getId());
      domain.setLastModifiedBy(actor.getId());
    }
  }

  @Override
  protected String getEntity() {
    return SystemModules.MASTER_DATA.getEntity();
  }

  @Override
  protected MasterDataDomain prepareResponse(MasterDataDomain domain) {
    return domain;
  }
}
