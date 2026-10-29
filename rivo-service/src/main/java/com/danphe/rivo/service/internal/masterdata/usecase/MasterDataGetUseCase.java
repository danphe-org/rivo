package com.danphe.rivo.service.internal.masterdata.usecase;

import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.library.base.service.api.UseCase;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.MasterDataRepository;

public class MasterDataGetUseCase implements UseCase<String, MasterDataDomain> {

  private final MasterDataRepository repository;
  private final UserDomain actor;

  public MasterDataGetUseCase(MasterDataRepository repository, UserDomain actor) {
    this.repository = repository;
    this.actor = actor;
  }

  @Override
  public MasterDataDomain execute(String id) {
    validate(id);
    return repository.findById(id)
        .filter(r -> !r.isDeleted())
        .orElseThrow(() -> new NotFoundException("Master data not found with id: " + id));
  }

  private void validate(String id) {
    if (id == null || id.isBlank()) {
      throw new AppException("Master data id is required");
    }
  }
}
