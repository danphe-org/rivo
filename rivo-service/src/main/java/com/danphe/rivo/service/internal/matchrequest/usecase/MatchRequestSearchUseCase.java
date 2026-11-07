package com.danphe.rivo.service.internal.matchrequest.usecase;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.service.api.SearchUseCase;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.matchrequest.MatchRequestFilter;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MatchRequestRepository;
import java.time.LocalDateTime;

public class MatchRequestSearchUseCase
    implements SearchUseCase<MatchRequestFilter, MatchRequestDomain> {

  private final MatchRequestRepository repository;
  private final UserDomain actor;

  public MatchRequestSearchUseCase(MatchRequestRepository repository, UserDomain actor) {
    this.repository = repository;
    this.actor = actor;
  }

  @Override
  public Page<MatchRequestDomain> execute(MatchRequestFilter filter) {
    if (filter == null) {
      filter = new MatchRequestFilter();
    }
    filter.setNow(LocalDateTime.now());
    if (actor != null && actor.isAdmin()) {
      filter.setAdmin(true);
    } else {
      filter.setAdmin(false);
      filter.setViewerId(actor != null ? actor.getId() : null);
    }
    return repository.findAllWithPagination(filter);
  }
}
