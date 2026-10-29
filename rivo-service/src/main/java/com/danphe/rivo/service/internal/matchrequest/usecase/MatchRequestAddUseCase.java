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

public class MatchRequestAddUseCase
    extends AbstractAddUseCase<MatchRequestDomain, MatchRequestDomain> {

  private final UserDomain actor;

  public MatchRequestAddUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      MatchRequestRepository repository,
      UserDomain actor) {
    super(converter, auditDao, repository);
    this.actor = actor;
  }

  @Override
  protected void validate(MatchRequestDomain req) {
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
