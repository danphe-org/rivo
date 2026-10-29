package com.danphe.rivo.service.internal.matchrequest.usecase;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.service.api.SearchUseCase;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.matchrequest.MatchRequestFilter;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MatchRequestRepository;

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
    return repository.findAllWithPagination(filter);
  }
}
