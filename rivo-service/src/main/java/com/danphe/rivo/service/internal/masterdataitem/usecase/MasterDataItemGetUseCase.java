package com.danphe.rivo.service.internal.masterdataitem.usecase;

import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.library.base.service.api.UseCase;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MasterDataItemRepository;

public class MasterDataItemGetUseCase implements UseCase<String, MasterDataItemDomain> {

  private final MasterDataItemRepository repository;
  private final UserDomain actor;

  public MasterDataItemGetUseCase(MasterDataItemRepository repository, UserDomain actor) {
    this.repository = repository;
    this.actor = actor;
  }

  @Override
  public MasterDataItemDomain execute(String id) {
    validate(id);
    return repository.findById(id)
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new NotFoundException("Master data item not found with id: " + id));
  }

  private void validate(String id) {
    if (id == null || id.isBlank()) {
      throw new AppException("Master data item id is required");
    }
  }
}
