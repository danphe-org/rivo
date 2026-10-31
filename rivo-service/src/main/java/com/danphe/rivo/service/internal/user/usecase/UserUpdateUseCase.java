package com.danphe.rivo.service.internal.user.usecase;

import com.danphe.rivo.common.abstracts.AbstractUpdateUseCase;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserRole;
import com.danphe.rivo.common.user.UserRoleDomain;
import com.danphe.rivo.repository.api.UserRepository;
import com.danphe.rivo.repository.api.UserRoleRepository;
import com.danphe.rivo.service.internal.user.PasswordValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserUpdateUseCase extends AbstractUpdateUseCase<UserDomain, UserDomain> {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern PHONE_PATTERN =
      Pattern.compile("^[+]?[0-9]{7,20}$");

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserDomain actor;

  public UserUpdateUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      UserRepository userRepository,
      UserRoleRepository userRoleRepository,
      PasswordEncoder passwordEncoder,
      UserDomain actor) {
    super(converter, auditDao, userRepository);
    this.userRepository = userRepository;
    this.userRoleRepository = userRoleRepository;
    this.passwordEncoder = passwordEncoder;
    this.actor = actor;
  }

  @Override
  protected void validate(UserDomain req) {
    if (req.getId() == null || req.getId().isBlank()) {
      throw new AppException("User id is required for update");
    }

    UserDomain oldUser = userRepository.findById(req.getId())
        .orElseThrow(() -> new NotFoundException("User not found with id: " + req.getId()));

    if (actor != null && !actor.isSystemUser()) {
      if (!req.getId().equalsIgnoreCase(actor.getId())) {
        throw new ForbiddenException("Access denied: You can only update your own data");
      }
      if (req.getRole() != null && !req.getRole().equalsIgnoreCase(oldUser.getRole())) {
        throw new ForbiddenException("Non-system users cannot change user roles");
      }
      if (req.isActive() != oldUser.isActive()) {
        throw new ForbiddenException("Non-system users cannot change account status");
      }
    }

    if (req.getName() != null) {
      String trimmedName = req.getName().trim();
      if (trimmedName.length() < 2 || trimmedName.length() > 100) {
        throw new AppException("Name must be between 2 and 100 characters");
      }
    }

    if (req.getEmail() != null) {
      String trimmedEmail = req.getEmail().trim().toLowerCase();
      if (!EMAIL_PATTERN.matcher(trimmedEmail).matches() || trimmedEmail.length() > 255) {
        throw new AppException("Invalid email format");
      }
      Optional<UserDomain> existing = userRepository.findByEmail(trimmedEmail);
      if (existing.isPresent() && !existing.get().getId().equalsIgnoreCase(req.getId())) {
        throw new AppException("User with email '" + trimmedEmail + "' already exists");
      }
    }

    if (req.getPhone() != null && !req.getPhone().trim().isBlank()) {
      String trimmedPhone = req.getPhone().trim();
      if (!PHONE_PATTERN.matcher(trimmedPhone).matches()) {
        throw new AppException("Phone number must contain between 7 and 20 digits");
      }
      Optional<UserDomain> existing = userRepository.findByPhone(trimmedPhone);
      if (existing.isPresent() && !existing.get().getId().equalsIgnoreCase(req.getId())) {
        throw new AppException("User with phone '" + trimmedPhone + "' already exists");
      }
    }

    if (req.getPassword() != null && !req.getPassword().isBlank()) {
      PasswordValidator.validate(req.getPassword());
    }
  }

  @Override
  protected void preProcessing(UserDomain domain, UserDomain oldDomain) {
    if (domain.getName() != null) {
      domain.setName(domain.getName().trim());
    } else {
      domain.setName(oldDomain.getName());
    }

    if (domain.getEmail() != null) {
      domain.setEmail(domain.getEmail().trim().toLowerCase());
    } else {
      domain.setEmail(oldDomain.getEmail());
    }

    if (domain.getPhone() != null) {
      domain.setPhone(domain.getPhone().trim());
    } else {
      domain.setPhone(oldDomain.getPhone());
    }

    if (domain.getPassword() != null && !domain.getPassword().isBlank()) {
      domain.setPassword(passwordEncoder.encode(domain.getPassword()));
    } else {
      domain.setPassword(oldDomain.getPassword());
    }

    if (domain.getRole() == null || domain.getRole().isBlank()) {
      domain.setRole(oldDomain.getRole());
    } else {
      domain.setRole(domain.getRole().trim().toUpperCase());
    }

    if (domain.getVerified() == null) {
      domain.setVerified(oldDomain.getVerified());
    }
  }

  @Override
  protected int update(UserDomain domain) {
    int count = this.userRepository.update(domain);

    if (domain.getRole() != null) {
      List<UserRoleDomain> roles = userRoleRepository.findByUserId(domain.getId());
      if (roles.isEmpty()) {
        UserRoleDomain ur = new UserRoleDomain();
        ur.setId(StringUtils.generateUUID());
        ur.setUserId(domain.getId());
        ur.setRole(domain.getRole());
        ur.setActive(true);
        ur.setDeleted(false);
        ur.setCreatedOn(LocalDateTime.now());
        ur.setCreatedBy(getUser());
        ur.setLastModifiedOn(LocalDateTime.now());
        ur.setLastModifiedBy(getUser());
        userRoleRepository.insert(ur);
      } else {
        UserRoleDomain primary = roles.get(0);
        primary.setRole(domain.getRole());
        primary.setLastModifiedOn(LocalDateTime.now());
        primary.setLastModifiedBy(getUser());
        userRoleRepository.update(primary);
      }
    }

    return count;
  }

  @Override
  protected String getEntity() {
    return SystemModules.USER.getEntity();
  }

  @Override
  protected UserDomain prepareResponse(UserDomain domain) {
    return userRepository.findById(domain.getId()).orElse(domain);
  }
}
