package com.danphe.rivo.service.internal.masterdata.usecase;

import com.danphe.rivo.common.abstracts.AbstractUpdateUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MasterDataRepository;
import java.time.LocalDateTime;

public class MasterDataUpdateUseCase
    extends AbstractUpdateUseCase<MasterDataDomain, MasterDataDomain> {

  private final MasterDataRepository repository;
  private final UserDomain actor;

  public MasterDataUpdateUseCase(
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
    MasterDataDomain old = repository.findById(req.getId())
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new NotFoundException("Master data not found with id: " + req.getId()));

    String targetCode = (req.getCode() != null && !req.getCode().isBlank())
        ? req.getCode().trim().toUpperCase()
        : old.getCode();
    String targetTitle = (req.getTitle() != null && !req.getTitle().isBlank())
        ? req.getTitle().trim()
        : old.getTitle();

    if (!targetCode.equalsIgnoreCase(old.getCode()) || !targetTitle.equalsIgnoreCase(old.getTitle())) {
      repository.findByCodeAndTitle(targetCode, targetTitle).ifPresent(existing -> {
        if (!existing.getId().equalsIgnoreCase(req.getId())) {
          throw new AppException("Master data with title '" + targetTitle + "' already exists under code '" + targetCode + "'");
        }
      });
    }
  }

  @Override
  protected void preProcessing(MasterDataDomain domain, MasterDataDomain oldDomain) {
    domain.setCreatedBy(oldDomain.getCreatedBy());
    domain.setCreatedOn(oldDomain.getCreatedOn());
    if (actor != null) {
      domain.setLastModifiedBy(actor.getId());
    }
    domain.setLastModifiedOn(LocalDateTime.now());

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
  }

  @Override
  protected String getEntity() {
    return SystemModules.MASTER_DATA.getEntity();
  }

  @Override
  protected MasterDataDomain prepareResponse(MasterDataDomain domain) {
    return repository.findById(domain.getId()).orElse(domain);
  }
}
