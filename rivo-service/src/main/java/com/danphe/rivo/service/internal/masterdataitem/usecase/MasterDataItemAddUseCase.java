package com.danphe.rivo.service.internal.masterdataitem.usecase;

import com.danphe.rivo.common.abstracts.AbstractAddUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MasterDataItemRepository;
import com.danphe.rivo.repository.api.MasterDataRepository;

public class MasterDataItemAddUseCase
    extends AbstractAddUseCase<MasterDataItemDomain, MasterDataItemDomain> {

  private final MasterDataItemRepository repository;
  private final MasterDataRepository masterDataRepository;
  private final UserDomain actor;

  public MasterDataItemAddUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      MasterDataItemRepository repository,
      MasterDataRepository masterDataRepository,
      UserDomain actor) {
    super(converter, auditDao, repository);
    this.repository = repository;
    this.masterDataRepository = masterDataRepository;
    this.actor = actor;
  }

  @Override
  protected void validate(MasterDataItemDomain req) {
    masterDataRepository.findById(req.getMasterDataId())
        .filter(m -> !m.isDeleted())
        .orElseThrow(() -> new NotFoundException("Master data not found with id: " + req.getMasterDataId()));

    String normalizedTitle = req.getTitle() != null ? req.getTitle().trim() : "";
    if (repository.findByMasterDataIdAndTitle(req.getMasterDataId(), normalizedTitle).isPresent()) {
      throw new AppException("Master data item with title '" + normalizedTitle + "' already exists under this master data");
    }
  }

  @Override
  protected void preProcessing(MasterDataItemDomain domain) {
    domain.setTitle(domain.getTitle().trim());
    if (domain.getCode() != null) {
      domain.setCode(domain.getCode().trim().toUpperCase());
    }
    if (domain.getDescription() != null) {
      domain.setDescription(domain.getDescription().trim());
    }
    if (actor != null) {
      domain.setCreatedBy(actor.getId());
      domain.setLastModifiedBy(actor.getId());
    }
  }

  @Override
  protected String getEntity() {
    return SystemModules.MASTER_DATA_ITEM.getEntity();
  }

  @Override
  protected MasterDataItemDomain prepareResponse(MasterDataItemDomain domain) {
    return domain;
  }
}
