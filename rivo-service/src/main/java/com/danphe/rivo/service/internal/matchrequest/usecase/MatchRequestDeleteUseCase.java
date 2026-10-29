package com.danphe.rivo.service.internal.matchrequest.usecase;

import com.danphe.rivo.common.abstracts.AbstractDeleteUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MatchRequestRepository;
import java.time.LocalDateTime;

public class MatchRequestDeleteUseCase extends AbstractDeleteUseCase<MatchRequestDomain, Void> {

  private final MatchRequestRepository repository;
  private final UserDomain actor;

  public MatchRequestDeleteUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      MatchRequestRepository repository,
      UserDomain actor) {
    super(converter, auditDao, repository);
    this.repository = repository;
    this.actor = actor;
  }

  @Override
  protected void validate(MatchRequestDomain req) {
    MatchRequestDomain existing = repository.findById(req.getId())
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new NotFoundException("Match request not found with id: " + req.getId()));

    if (actor != null && !actor.isSystemUser()) {
      if (existing.getRequestedBy() == null || !existing.getRequestedBy().equalsIgnoreCase(actor.getId())) {
        throw new ForbiddenException("Access denied: You can only delete match requests you have created");
      }
    }
  }

  @Override
  protected void preProcessing(MatchRequestDomain domain, MatchRequestDomain oldDomain) {
    if (actor != null) {
      domain.setDeletedBy(actor.getId());
    }
    domain.setDeletedOn(LocalDateTime.now());
    domain.setActive(false);
    domain.setDeleted(true);
  }

  @Override
  protected String getEntity() {
    return SystemModules.MATCH_REQUEST.getEntity();
  }

  @Override
  protected Void prepareResponse(MatchRequestDomain domain) {
    return null;
  }
}
