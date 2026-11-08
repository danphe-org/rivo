package com.danphe.rivo.service.internal.matchrequest.usecase;

import com.danphe.rivo.common.abstracts.AbstractAddUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.matchrequest.MatchRequestStatus;
import com.danphe.rivo.common.matchrequest.MatchRequestType;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MatchRequestRepository;
import com.danphe.rivo.service.internal.matchrequest.validator.MatchRequestMasterDataValidator;

public class MatchRequestAddUseCase
    extends AbstractAddUseCase<MatchRequestDomain, MatchRequestDomain> {

  private final MatchRequestMasterDataValidator masterDataValidator;
  private final UserDomain actor;

  public MatchRequestAddUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      MatchRequestRepository repository,
      MatchRequestMasterDataValidator masterDataValidator,
      UserDomain actor) {
    super(converter, auditDao, repository);
    this.masterDataValidator = masterDataValidator;
    this.actor = actor;
  }

  @Override
  protected void validate(MatchRequestDomain req) {
    if (masterDataValidator != null) {
      MatchRequestMasterDataValidator.ValidatedResult validated =
          masterDataValidator.validate(req.getCategory(), req.getGame());
      req.setCategory(validated.category());
      req.setGame(validated.game());
    }
  }

  @Override
  protected void preProcessing(MatchRequestDomain domain) {
    if (actor != null) {
      domain.setRequestedBy(actor.getId());
      domain.setCreatedBy(actor.getId());
      domain.setLastModifiedBy(actor.getId());
    }
    domain.setStatus(MatchRequestStatus.OPEN.name());

    if (domain.getRequestType() != null) {
      domain.setRequestType(MatchRequestType.fromString(domain.getRequestType()).name());
    }
    if (domain.getGame() != null) {
      domain.setGame(domain.getGame().trim());
    }
    if (domain.getCategory() != null) {
      domain.setCategory(domain.getCategory().trim());
    }
    if (domain.getMessage() != null) {
      domain.setMessage(domain.getMessage().trim());
    }
    if (domain.getLocation() != null) {
      domain.setLocation(domain.getLocation().trim());
    }
    if (MatchRequestType.TEAMMATE.name().equalsIgnoreCase(domain.getRequestType())) {
      if (domain.getPlayersNeeded() == null || domain.getPlayersNeeded() <= 0) {
        domain.setPlayersNeeded(1);
      }
    } else {
      domain.setPlayersNeeded(null);
    }
  }

  @Override
  protected String getEntity() {
    return SystemModules.MATCH_REQUEST.getEntity();
  }

  @Override
  protected MatchRequestDomain prepareResponse(MatchRequestDomain domain) {
    if (actor != null) {
      domain.setRequestedByName(actor.getName());
      domain.setRequestedByEmail(actor.getEmail());
    }
    return domain;
  }
}
