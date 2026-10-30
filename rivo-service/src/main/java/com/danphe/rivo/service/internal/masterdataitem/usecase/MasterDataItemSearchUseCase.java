package com.danphe.rivo.service.internal.masterdataitem.usecase;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.service.api.SearchUseCase;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.masterdataitem.MasterDataItemFilter;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MasterDataItemRepository;

public class MasterDataItemSearchUseCase
    implements SearchUseCase<MasterDataItemFilter, MasterDataItemDomain> {

  private final MasterDataItemRepository repository;
  private final UserDomain actor;

  public MasterDataItemSearchUseCase(MasterDataItemRepository repository, UserDomain actor) {
    this.repository = repository;
    this.actor = actor;
  }

  @Override
  public Page<MasterDataItemDomain> execute(MasterDataItemFilter filter) {
    return repository.findAllWithPagination(filter);
  }
}
