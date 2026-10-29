package com.danphe.rivo.service.internal.masterdata;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.masterdata.MasterDataFilter;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.AuditDao;
import com.danphe.rivo.repository.api.MasterDataRepository;
import com.danphe.rivo.repository.internal.masterdata.MasterDataEntityConverter;
import com.danphe.rivo.service.api.MasterDataService;
import com.danphe.rivo.service.internal.masterdata.usecase.MasterDataAddUseCase;
import com.danphe.rivo.service.internal.masterdata.usecase.MasterDataDeleteUseCase;
import com.danphe.rivo.service.internal.masterdata.usecase.MasterDataGetUseCase;
import com.danphe.rivo.service.internal.masterdata.usecase.MasterDataSearchUseCase;
import com.danphe.rivo.service.internal.masterdata.usecase.MasterDataUpdateUseCase;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MasterDataServiceImpl implements MasterDataService {

  private final MasterDataRepository repository;
  private final MasterDataEntityConverter converter;
  private final AuditDao auditDao;

  public MasterDataServiceImpl(
      MasterDataRepository repository,
      MasterDataEntityConverter converter,
      AuditDao auditDao) {
    this.repository = repository;
    this.converter = converter;
    this.auditDao = auditDao;
  }

  @Override
  public MasterDataDomain insert(MasterDataDomain domain, UserDomain actor) {
    return new MasterDataAddUseCase(converter, auditDao, repository, actor).execute(domain);
  }

  @Override
  public MasterDataDomain getById(String id, UserDomain actor) {
    return new MasterDataGetUseCase(repository, actor).execute(id);
  }

  @Override
  public List<MasterDataDomain> getByCode(String code, UserDomain actor) {
    return repository.findByCode(code);
  }

  @Override
  public MasterDataDomain update(MasterDataDomain domain, UserDomain actor) {
    return new MasterDataUpdateUseCase(converter, auditDao, repository, actor).execute(domain);
  }

  @Override
  public void delete(String id, UserDomain actor) {
    MasterDataDomain domain = new MasterDataDomain();
    domain.setId(id);
    new MasterDataDeleteUseCase(converter, auditDao, repository, actor).execute(domain);
  }

  @Override
  public Page<MasterDataDomain> search(MasterDataFilter filter, UserDomain actor) {
    return new MasterDataSearchUseCase(repository, actor).execute(filter);
  }
}
