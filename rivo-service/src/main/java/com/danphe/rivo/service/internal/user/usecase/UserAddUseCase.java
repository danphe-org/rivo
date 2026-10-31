package com.danphe.rivo.service.internal.user.usecase;

import com.danphe.rivo.common.abstracts.AbstractAddUseCase;
import com.danphe.rivo.common.audits.AuditDomain;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import com.danphe.rivo.common.library.base.dao.entity.EntityConverter;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.common.modules.SystemModules;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserRole;
import com.danphe.rivo.common.user.UserRoleDomain;
import com.danphe.rivo.repository.api.UserRepository;
import com.danphe.rivo.service.internal.user.PasswordValidator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserAddUseCase extends AbstractAddUseCase<UserDomain, UserDomain> {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern PHONE_PATTERN =
      Pattern.compile("^[+]?[0-9]{7,20}$");

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserDomain actor;

  public UserAddUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this(converter, auditDao, userRepository, passwordEncoder, null);
  }

  public UserAddUseCase(
      EntityConverter converter,
      AbstractDao auditDao,
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      UserDomain actor) {
    super(converter, auditDao, userRepository);
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.actor = actor;
  }

  @Override
  protected void validate(UserDomain req) {
    if (actor != null && !actor.isSystemUser()) {
      throw new ForbiddenException("Access denied: Non-system users cannot create users");
    }

    if (req.getName() == null || req.getName().trim().isBlank()) {
      throw new AppException("Name is required");
    }
    String trimmedName = req.getName().trim();
    if (trimmedName.length() < 2 || trimmedName.length() > 100) {
      throw new AppException("Name must be between 2 and 100 characters");
    }

    if (req.getEmail() == null || req.getEmail().trim().isBlank()) {
      throw new AppException("Email is required");
    }
    String trimmedEmail = req.getEmail().trim().toLowerCase();
    if (!EMAIL_PATTERN.matcher(trimmedEmail).matches() || trimmedEmail.length() > 255) {
      throw new AppException("Invalid email format");
    }

    if (userRepository.findByEmail(trimmedEmail).isPresent()) {
      throw new AppException("User with email '" + trimmedEmail + "' already exists");
    }

    if (req.getPhone() != null && !req.getPhone().trim().isBlank()) {
      String trimmedPhone = req.getPhone().trim();
      if (!PHONE_PATTERN.matcher(trimmedPhone).matches()) {
        throw new AppException("Phone number must contain between 7 and 20 digits");
      }
      if (userRepository.findByPhone(trimmedPhone).isPresent()) {
        throw new AppException("User with phone '" + trimmedPhone + "' already exists");
      }
    }

    if (req.getPassword() != null && !req.getPassword().isBlank()) {
      PasswordValidator.validate(req.getPassword());
    }
  }

  @Override
  protected void preProcessing(UserDomain domain) {
    domain.setName(domain.getName().trim());
    domain.setEmail(domain.getEmail().trim().toLowerCase());
    if (domain.getPhone() != null && !domain.getPhone().trim().isBlank()) {
      domain.setPhone(domain.getPhone().trim());
    } else {
      domain.setPhone(null);
    }

    if (domain.getPassword() != null && !domain.getPassword().isBlank()) {
      domain.setPassword(passwordEncoder.encode(domain.getPassword()));
    }

    if (domain.getVerified() == null) {
      domain.setVerified(false);
    }

    UserRole assignedRole = domain.getRole() != null && !domain.getRole().isBlank()
        ? UserRole.fromString(domain.getRole())
        : UserRole.CLIENT;

    String roleName = assignedRole.name();
    domain.setRole(roleName);

    if (domain.getRoles() == null || domain.getRoles().isEmpty()) {
      UserRoleDomain userRole = new UserRoleDomain();
      userRole.setId(StringUtils.generateUUID());
      userRole.setUserId(domain.getId());
      userRole.setRole(roleName);
      userRole.setActive(true);
      userRole.setDeleted(false);
      userRole.setCreatedOn(LocalDateTime.now());
      userRole.setCreatedBy(getUser());
      userRole.setLastModifiedOn(LocalDateTime.now());
      userRole.setLastModifiedBy(getUser());
      userRole.setVersion(1L);
      List<UserRoleDomain> rolesList = new ArrayList<>();
      rolesList.add(userRole);
      domain.setRoles(rolesList);
    } else {
      for (UserRoleDomain r : domain.getRoles()) {
        if (r.getId() == null || r.getId().isBlank()) {
          r.setId(StringUtils.generateUUID());
        }
        r.setUserId(domain.getId());
        r.setCreatedOn(LocalDateTime.now());
        r.setCreatedBy(getUser());
        r.setLastModifiedOn(LocalDateTime.now());
        r.setLastModifiedBy(getUser());
        r.setActive(true);
        r.setDeleted(false);
        r.setVersion(1L);
      }
    }
  }

  @Override
  protected String getEntity() {
    return SystemModules.USER.getEntity();
  }

  @Override
  protected UserDomain prepareResponse(UserDomain domain) {
    return domain;
  }
}
