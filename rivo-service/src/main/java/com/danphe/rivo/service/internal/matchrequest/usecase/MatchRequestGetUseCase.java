package com.danphe.rivo.service.internal.matchrequest.usecase;

import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.library.base.service.api.UseCase;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MatchRequestRepository;
import java.time.LocalDateTime;

public class MatchRequestGetUseCase implements UseCase<String, MatchRequestDomain> {

  private final MatchRequestRepository repository;
  private final UserDomain actor;

  public MatchRequestGetUseCase(MatchRequestRepository repository, UserDomain actor) {
    this.repository = repository;
    this.actor = actor;
  }

  @Override
  public MatchRequestDomain execute(String id) {
    validate(id);
    MatchRequestDomain domain = repository.findById(id)
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new NotFoundException("Match request not found with id: " + id));

    if (actor != null && actor.isAdmin()) {
      return domain;
    }

    if (isPast(domain)) {
      boolean isOwner = actor != null
          && actor.getId() != null
          && domain.getRequestedBy() != null
          && domain.getRequestedBy().equalsIgnoreCase(actor.getId());
      if (!isOwner) {
        throw new ForbiddenException("Access denied: You cannot view past match requests posted by other users");
      }
    }

    return domain;
  }

  private void validate(String id) {
    if (id == null || id.isBlank()) {
      throw new AppException("Match request id is required");
    }
  }

  private boolean isPast(MatchRequestDomain domain) {
    return domain.getTime() != null && domain.getTime().isBefore(LocalDateTime.now());
  }
}
