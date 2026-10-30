package com.danphe.rivo.service.internal.masterdataitem.usecase;

import com.danphe.rivo.common.abstracts.AbstractUpdateUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MasterDataItemRepository;
import com.danphe.rivo.repository.api.MasterDataRepository;
import java.time.LocalDateTime;

public class MasterDataItemUpdateUseCase
    extends AbstractUpdateUseCase<MasterDataItemDomain, MasterDataItemDomain> {

  private final MasterDataItemRepository repository;
  private final MasterDataRepository masterDataRepository;
  private final UserDomain actor;

  public MasterDataItemUpdateUseCase(
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
    MasterDataItemDomain old = repository.findById(req.getId())
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new NotFoundException("Master data item not found with id: " + req.getId()));

    String targetMasterDataId = (req.getMasterDataId() != null && !req.getMasterDataId().isBlank())
        ? req.getMasterDataId()
        : old.getMasterDataId();

    if (!targetMasterDataId.equalsIgnoreCase(old.getMasterDataId())) {
      masterDataRepository.findById(targetMasterDataId)
          .filter(m -> !m.isDeleted())
          .orElseThrow(() -> new NotFoundException("Master data not found with id: " + targetMasterDataId));
    }

    String targetTitle = (req.getTitle() != null && !req.getTitle().isBlank())
        ? req.getTitle().trim()
        : old.getTitle();

    if (!targetMasterDataId.equalsIgnoreCase(old.getMasterDataId()) || !targetTitle.equalsIgnoreCase(old.getTitle())) {
      repository.findByMasterDataIdAndTitle(targetMasterDataId, targetTitle).ifPresent(existing -> {
        if (!existing.getId().equalsIgnoreCase(req.getId())) {
          throw new AppException("Master data item with title '" + targetTitle + "' already exists under this master data");
        }
      });
    }
  }

  @Override
  protected void preProcessing(MasterDataItemDomain domain, MasterDataItemDomain oldDomain) {
    domain.setCreatedBy(oldDomain.getCreatedBy());
    domain.setCreatedOn(oldDomain.getCreatedOn());
    if (actor != null) {
      domain.setLastModifiedBy(actor.getId());
    }
    domain.setLastModifiedOn(LocalDateTime.now());

    if (domain.getMasterDataId() == null || domain.getMasterDataId().isBlank()) {
      domain.setMasterDataId(oldDomain.getMasterDataId());
    }

    if (domain.getTitle() == null) {
      domain.setTitle(oldDomain.getTitle());
    } else {
      domain.setTitle(domain.getTitle().trim());
    }

    if (domain.getCode() == null || domain.getCode().isBlank()) {
      domain.setCode(oldDomain.getCode());
    } else {
      domain.setCode(domain.getCode().trim().toUpperCase());
    }

    if (domain.getDescription() == null) {
      domain.setDescription(oldDomain.getDescription());
    } else {
      domain.setDescription(domain.getDescription().trim());
    }
  }

  @Override
  protected String getEntity() {
    return SystemModules.MASTER_DATA_ITEM.getEntity();
  }

  @Override
  protected MasterDataItemDomain prepareResponse(MasterDataItemDomain domain) {
    return repository.findById(domain.getId()).orElse(domain);
  }
}
