package com.danphe.rivo.service.internal.user.usecase;

import com.danphe.rivo.common.abstracts.AbstractDeleteUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.UserRepository;

public class UserDeleteUseCase extends AbstractDeleteUseCase<UserDomain, Void> {

  private final UserRepository userRepository;
  private final UserDomain actor;

  public UserDeleteUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      UserRepository userRepository,
      UserDomain actor) {
    super(converter, auditDao, userRepository);
    this.userRepository = userRepository;
    this.actor = actor;
  }

  @Override
  protected void validate(UserDomain req) {
    if (req.getId() == null || req.getId().isBlank()) {
      throw new AppException("User id is required for delete");
    }

    userRepository.findById(req.getId())
        .orElseThrow(() -> new NotFoundException("User not found with id: " + req.getId()));

    if (actor != null && !actor.isSystemUser()) {
      if (!req.getId().equalsIgnoreCase(actor.getId())) {
        throw new ForbiddenException("Access denied: You cannot delete another user");
      }
    }
  }

  @Override
  protected String getEntity() {
    return SystemModules.USER.getEntity();
  }

  @Override
  protected Void prepareResponse(UserDomain domain) {
    return null;
  }
}
