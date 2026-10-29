package com.danphe.rivo.service.internal.matchrequest;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.matchrequest.MatchRequestFilter;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.AuditDao;
import com.danphe.rivo.repository.api.MatchRequestRepository;
import com.danphe.rivo.repository.internal.matchrequest.MatchRequestEntityConverter;
import com.danphe.rivo.service.api.MatchRequestService;
import com.danphe.rivo.service.internal.matchrequest.usecase.MatchRequestAddUseCase;
import com.danphe.rivo.service.internal.matchrequest.usecase.MatchRequestDeleteUseCase;
import com.danphe.rivo.service.internal.matchrequest.usecase.MatchRequestGetUseCase;
import com.danphe.rivo.service.internal.matchrequest.usecase.MatchRequestSearchUseCase;
import com.danphe.rivo.service.internal.matchrequest.usecase.MatchRequestUpdateUseCase;
import org.springframework.stereotype.Service;

@Service
public class MatchRequestServiceImpl implements MatchRequestService {

  private final MatchRequestRepository repository;
  private final MatchRequestEntityConverter converter;
  private final AuditDao auditDao;

  public MatchRequestServiceImpl(
      MatchRequestRepository repository,
      MatchRequestEntityConverter converter,
      AuditDao auditDao) {
    this.repository = repository;
    this.converter = converter;
    this.auditDao = auditDao;
  }

  @Override
  public MatchRequestDomain insert(MatchRequestDomain domain, UserDomain actor) {
    return new MatchRequestAddUseCase(converter, auditDao, repository, actor).execute(domain);
  }

  @Override
  public MatchRequestDomain getById(String id, UserDomain actor) {
    return new MatchRequestGetUseCase(repository, actor).execute(id);
  }

  @Override
  public MatchRequestDomain update(MatchRequestDomain domain, UserDomain actor) {
    return new MatchRequestUpdateUseCase(converter, auditDao, repository, actor).execute(domain);
  }

  @Override
  public void delete(String id, UserDomain actor) {
    MatchRequestDomain domain = new MatchRequestDomain();
    domain.setId(id);
    new MatchRequestDeleteUseCase(converter, auditDao, repository, actor).execute(domain);
  }

  @Override
  public Page<MatchRequestDomain> search(MatchRequestFilter filter, UserDomain actor) {
    return new MatchRequestSearchUseCase(repository, actor).execute(filter);
  }
}
