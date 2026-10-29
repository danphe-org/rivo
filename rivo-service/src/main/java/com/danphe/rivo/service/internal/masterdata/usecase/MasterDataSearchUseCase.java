package com.danphe.rivo.service.internal.masterdata.usecase;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.service.api.SearchUseCase;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.masterdata.MasterDataFilter;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MasterDataRepository;

public class MasterDataSearchUseCase
    implements SearchUseCase<MasterDataFilter, MasterDataDomain> {

  private final MasterDataRepository repository;
  private final UserDomain actor;

  public MasterDataSearchUseCase(MasterDataRepository repository, UserDomain actor) {
    this.repository = repository;
    this.actor = actor;
  }

  @Override
  public Page<MasterDataDomain> execute(MasterDataFilter filter) {
    return repository.findAllWithPagination(filter);
  }
}
