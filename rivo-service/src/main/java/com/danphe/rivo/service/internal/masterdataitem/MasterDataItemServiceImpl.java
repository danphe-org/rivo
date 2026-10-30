package com.danphe.rivo.service.internal.masterdataitem;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.masterdataitem.MasterDataItemFilter;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.AuditDao;
import com.danphe.rivo.repository.api.MasterDataItemRepository;
import com.danphe.rivo.repository.api.MasterDataRepository;
import com.danphe.rivo.repository.internal.masterdataitem.MasterDataItemEntityConverter;
import com.danphe.rivo.service.api.MasterDataItemService;
import com.danphe.rivo.service.internal.masterdataitem.usecase.MasterDataItemAddUseCase;
import com.danphe.rivo.service.internal.masterdataitem.usecase.MasterDataItemDeleteUseCase;
import com.danphe.rivo.service.internal.masterdataitem.usecase.MasterDataItemGetUseCase;
import com.danphe.rivo.service.internal.masterdataitem.usecase.MasterDataItemSearchUseCase;
import com.danphe.rivo.service.internal.masterdataitem.usecase.MasterDataItemUpdateUseCase;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MasterDataItemServiceImpl implements MasterDataItemService {

  private final MasterDataItemRepository repository;
  private final MasterDataRepository masterDataRepository;
  private final MasterDataItemEntityConverter converter;
  private final AuditDao auditDao;

  public MasterDataItemServiceImpl(
      MasterDataItemRepository repository,
      MasterDataRepository masterDataRepository,
      MasterDataItemEntityConverter converter,
      AuditDao auditDao) {
    this.repository = repository;
    this.masterDataRepository = masterDataRepository;
    this.converter = converter;
    this.auditDao = auditDao;
  }

  @Override
  public MasterDataItemDomain insert(MasterDataItemDomain domain, UserDomain actor) {
    return new MasterDataItemAddUseCase(converter, auditDao, repository, masterDataRepository, actor).execute(domain);
  }

  @Override
  public MasterDataItemDomain getById(String id, UserDomain actor) {
    return new MasterDataItemGetUseCase(repository, actor).execute(id);
  }

  @Override
  public List<MasterDataItemDomain> getByMasterDataId(String masterDataId, UserDomain actor) {
    return repository.findByMasterDataId(masterDataId);
  }

  @Override
  public MasterDataItemDomain update(MasterDataItemDomain domain, UserDomain actor) {
    return new MasterDataItemUpdateUseCase(converter, auditDao, repository, masterDataRepository, actor).execute(domain);
  }

  @Override
  public void delete(String id, UserDomain actor) {
    MasterDataItemDomain domain = new MasterDataItemDomain();
    domain.setId(id);
    new MasterDataItemDeleteUseCase(converter, auditDao, repository, actor).execute(domain);
  }

  @Override
  public Page<MasterDataItemDomain> search(MasterDataItemFilter filter, UserDomain actor) {
    return new MasterDataItemSearchUseCase(repository, actor).execute(filter);
  }
}
