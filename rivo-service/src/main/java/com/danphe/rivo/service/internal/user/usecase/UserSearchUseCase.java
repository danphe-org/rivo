package com.danphe.rivo.service.internal.user.usecase;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.base.service.api.SearchUseCase;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserFilter;
import com.danphe.rivo.repository.api.UserRepository;

public class UserSearchUseCase implements SearchUseCase<UserFilter, UserDomain> {

  private final UserRepository userRepository;
  private final UserDomain actor;

  public UserSearchUseCase(UserRepository userRepository) {
    this(userRepository, null);
  }

  public UserSearchUseCase(UserRepository userRepository, UserDomain actor) {
    this.userRepository = userRepository;
    this.actor = actor;
  }

  @Override
  public Page<UserDomain> execute(UserFilter filter) {
    validate();
    return userRepository.findAllWithPagination(filter);
  }

  private void validate() {
    if (actor != null && !actor.isSystemUser()) {
      throw new ForbiddenException("Access denied: Non-system users cannot search users");
    }
  }
}
