package com.danphe.rivo.service.internal.user;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserFilter;
import com.danphe.rivo.common.user.UserRole;
import com.danphe.rivo.repository.api.AuditDao;
import com.danphe.rivo.repository.api.UserRepository;
import com.danphe.rivo.repository.api.UserRoleRepository;
import com.danphe.rivo.repository.internal.user.UserEntityConverter;
import com.danphe.rivo.service.api.UserService;
import com.danphe.rivo.service.internal.user.usecase.UserAddUseCase;
import com.danphe.rivo.service.internal.user.usecase.UserDeleteUseCase;
import com.danphe.rivo.service.internal.user.usecase.UserGetUseCase;
import com.danphe.rivo.service.internal.user.usecase.UserSearchUseCase;
import com.danphe.rivo.service.internal.user.usecase.UserUpdateUseCase;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Primary
@Service("userService")
public class UserServiceImpl implements UserService {

  private final UserRepository repository;
  private final UserRoleRepository userRoleRepository;
  private final UserEntityConverter converter;
  private final AuditDao auditDao;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(
      @Qualifier("systemUserRepository") UserRepository repository,
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
    UserAddUseCase useCase = new UserAddUseCase(this.converter, this.auditDao, this.repository, this.passwordEncoder);
    return useCase.execute(domain);
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
