package com.danphe.rivo.service.internal.matchrequest.usecase;

import com.danphe.rivo.common.abstracts.AbstractUpdateUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.matchrequest.MatchRequestStatus;
import com.danphe.rivo.common.matchrequest.MatchRequestType;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MatchRequestRepository;
import com.danphe.rivo.service.internal.matchrequest.validator.MatchRequestMasterDataValidator;
import java.time.LocalDateTime;

public class MatchRequestUpdateUseCase
    extends AbstractUpdateUseCase<MatchRequestDomain, MatchRequestDomain> {

  private final MatchRequestRepository repository;
  private final MatchRequestMasterDataValidator masterDataValidator;
  private final UserDomain actor;
  private MatchRequestDomain existing;

  public MatchRequestUpdateUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      MatchRequestRepository repository,
      MatchRequestMasterDataValidator masterDataValidator,
      UserDomain actor) {
    super(converter, auditDao, repository);
    this.repository = repository;
    this.masterDataValidator = masterDataValidator;
    this.actor = actor;
  }

  @Override
  protected void validate(MatchRequestDomain req) {
    this.existing = repository.findById(req.getId())
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new NotFoundException("Match request not found with id: " + req.getId()));

    if (actor != null && !actor.isSystemUser()) {
      if (existing.getRequestedBy() == null || !existing.getRequestedBy().equalsIgnoreCase(actor.getId())) {
        throw new ForbiddenException("Access denied: You can only update match requests you have created");
      }
    }

    String categoryToValidate = req.getCategory() != null ? req.getCategory() : existing.getCategory();
    String gameToValidate = req.getGame() != null ? req.getGame() : existing.getGame();

    if (masterDataValidator != null && (req.getCategory() != null || req.getGame() != null)) {
      MatchRequestMasterDataValidator.ValidatedResult validated =
          masterDataValidator.validate(categoryToValidate, gameToValidate);
      req.setCategory(validated.category());
      req.setGame(validated.game());
    }
  }

  @Override
  protected void preProcessing(MatchRequestDomain domain, MatchRequestDomain oldDomain) {
    domain.setRequestedBy(oldDomain.getRequestedBy());
    domain.setCreatedBy(oldDomain.getCreatedBy());
    domain.setCreatedOn(oldDomain.getCreatedOn());
    if (actor != null) {
      domain.setLastModifiedBy(actor.getId());
    }
    domain.setLastModifiedOn(LocalDateTime.now());

    if (domain.getGame() == null) {
      domain.setGame(oldDomain.getGame());
    } else {
      domain.setGame(domain.getGame().trim());
    }
    if (domain.getCategory() == null) {
      domain.setCategory(oldDomain.getCategory());
    } else {
      domain.setCategory(domain.getCategory().trim());
    }
    if (domain.getRequestType() == null) {
      domain.setRequestType(oldDomain.getRequestType());
    } else {
      domain.setRequestType(MatchRequestType.fromString(domain.getRequestType()).name());
    }
    if (domain.getStatus() == null) {
      domain.setStatus(oldDomain.getStatus());
    } else {
      domain.setStatus(MatchRequestStatus.fromString(domain.getStatus()).name());
    }
    if (domain.getMessage() == null) {
      domain.setMessage(oldDomain.getMessage());
    } else {
      domain.setMessage(domain.getMessage().trim());
    }
    if (domain.getLocation() == null) {
      domain.setLocation(oldDomain.getLocation());
    } else {
      domain.setLocation(domain.getLocation().trim());
    }
    if (MatchRequestType.TEAMMATE.name().equalsIgnoreCase(domain.getRequestType())) {
      if (domain.getPlayersNeeded() == null || domain.getPlayersNeeded() <= 0) {
        domain.setPlayersNeeded(oldDomain.getPlayersNeeded() != null && oldDomain.getPlayersNeeded() > 0 ? oldDomain.getPlayersNeeded() : 1);
      }
    } else {
      domain.setPlayersNeeded(null);
    }
    if (domain.getTime() == null) {
      domain.setTime(oldDomain.getTime());
    }
  }

  @Override
  protected String getEntity() {
    return SystemModules.MATCH_REQUEST.getEntity();
  }

  @Override
  protected MatchRequestDomain prepareResponse(MatchRequestDomain domain) {
    return repository.findById(domain.getId()).orElse(domain);
  }
}
