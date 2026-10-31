package com.danphe.rivo.service.internal.clientuser;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserFilter;
import com.danphe.rivo.common.user.UserRole;
import com.danphe.rivo.repository.api.AuditDao;
import com.danphe.rivo.repository.api.ClientUserRepository;
import com.danphe.rivo.repository.api.UserRoleRepository;
import com.danphe.rivo.repository.internal.user.UserEntityConverter;
import com.danphe.rivo.service.api.ClientUserService;
import com.danphe.rivo.service.internal.user.usecase.UserAddUseCase;
import com.danphe.rivo.service.internal.user.usecase.UserDeleteUseCase;
import com.danphe.rivo.service.internal.user.usecase.UserGetUseCase;
import com.danphe.rivo.service.internal.user.usecase.UserSearchUseCase;
import com.danphe.rivo.service.internal.user.usecase.UserUpdateUseCase;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("clientUserService")
public class ClientUserServiceImpl implements ClientUserService {

  private final ClientUserRepository repository;
  private final UserRoleRepository userRoleRepository;
  private final UserEntityConverter converter;
  private final AuditDao auditDao;
  private final PasswordEncoder passwordEncoder;

  public ClientUserServiceImpl(
      @Qualifier("clientUserRepository") ClientUserRepository repository,
      UserRoleRepository userRoleRepository,
      UserEntityConverter converter,
      AuditDao auditDao,
      PasswordEncoder passwordEncoder) {
    this.repository = repository;
    this.userRoleRepository = userRoleRepository;
    this.converter = converter;
    this.auditDao = auditDao;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserDomain insert(UserDomain domain) {
    return insert(domain, null);
  }

  @Override
  public UserDomain insert(UserDomain domain, UserDomain actor) {
    UserAddUseCase useCase = new UserAddUseCase(this.converter, this.auditDao, this.repository, this.passwordEncoder, actor);
    return useCase.execute(domain);
  }

  @Override
  public UserDomain register(UserDomain domain) {
    domain.setRole(UserRole.CLIENT.name());
    domain.setCreatedBy("SELF_REGISTRATION");
    domain.setLastModifiedBy("SELF_REGISTRATION");
    domain.setRemark("User self-registration");
    domain.setVerified(false);
    UserAddUseCase useCase = new UserAddUseCase(this.converter, this.auditDao, this.repository, this.passwordEncoder);
    return useCase.execute(domain);
  }

  @Override
  public void markVerified(String userId) {
    UserDomain user = repository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    user.setVerified(true);
    user.setActive(true);
    user.setLastModifiedOn(LocalDateTime.now());
    user.setLastModifiedBy("OTP_VERIFICATION");
    repository.update(user);
  }

  @Override
  public UserDomain update(UserDomain domain, UserDomain actor) {
    UserUpdateUseCase useCase = new UserUpdateUseCase(
        this.converter, this.auditDao, this.repository, this.userRoleRepository, this.passwordEncoder, actor);
    return useCase.execute(domain);
  }

  @Override
  public void delete(String id, UserDomain actor) {
    UserDomain domain = new UserDomain();
    domain.setId(id);
    UserDeleteUseCase useCase = new UserDeleteUseCase(this.converter, this.auditDao, this.repository, actor);
    useCase.execute(domain);
  }

  @Override
  public Optional<UserDomain> findById(String id) {
    return repository.findById(id);
  }

  @Override
  public UserDomain getById(String id, UserDomain actor) {
    UserGetUseCase useCase = new UserGetUseCase(this.repository, actor);
    return useCase.execute(id);
  }

  @Override
  public Optional<UserDomain> findByEmail(String email) {
    return repository.findByEmail(email);
  }

  @Override
  public Optional<UserDomain> findByPhone(String phone) {
    return repository.findByPhone(phone);
  }

  @Override
  public Optional<UserDomain> findOne(UserFilter filter) {
    return repository.findOne(filter);
  }

  @Override
  public List<UserDomain> findAllWithoutPagination(UserFilter filter) {
    return repository.findAll(filter);
  }

  @Override
  public Page<UserDomain> findAll(UserFilter filter) {
    return findAll(filter, null);
  }

  @Override
  public Page<UserDomain> findAll(UserFilter filter, UserDomain actor) {
    UserSearchUseCase useCase = new UserSearchUseCase(this.repository, actor);
    return useCase.execute(filter);
  }
}
