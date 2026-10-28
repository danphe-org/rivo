package com.danphe.rivo.service.internal.user.usecase;

import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.library.base.service.api.UseCase;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.repository.api.UserRepository;

public class UserGetUseCase implements UseCase<String, UserDomain> {

  private final UserRepository userRepository;
  private final UserDomain actor;

  public UserGetUseCase(UserRepository userRepository, UserDomain actor) {
    this.userRepository = userRepository;
    this.actor = actor;
  }

  @Override
  public UserDomain execute(String userId) {
    validate(userId);
    return userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
  }

  private void validate(String userId) {
    if (userId == null || userId.isBlank()) {
      throw new AppException("User id is required");
    }

    if (actor != null && !actor.isSystemUser()) {
      if (!userId.equalsIgnoreCase(actor.getId())) {
        throw new ForbiddenException("Access denied: You can only read your own data");
      }
    }
  }
}
