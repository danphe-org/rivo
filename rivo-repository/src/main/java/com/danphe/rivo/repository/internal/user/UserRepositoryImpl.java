package com.danphe.rivo.repository.internal.user;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserFilter;
import com.danphe.rivo.common.user.UserRoleDomain;
import com.danphe.rivo.repository.api.UserRepository;
import com.danphe.rivo.repository.api.UserRoleRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

  private final UserMapper mapper;
  private final UserEntityConverter converter;
  private final UserRoleRepository userRoleRepository;

  public UserRepositoryImpl(
      UserMapper mapper,
      UserEntityConverter converter,
      UserRoleRepository userRoleRepository) {
    this.mapper = mapper;
    this.converter = converter;
    this.userRoleRepository = userRoleRepository;
  }

  @Override
  public int insert(UserDomain domain) {
    if (domain.getId() == null || domain.getId().isBlank()) {
      domain.setId(StringUtils.generateUUID());
    }
    if (domain.getCreatedOn() == null) {
      domain.setCreatedOn(LocalDateTime.now());
    }
    if (domain.getLastModifiedOn() == null) {
      domain.setLastModifiedOn(LocalDateTime.now());
    }
    int count = mapper.insert(converter.toEntity(domain));

    if (domain.getRoles() != null && !domain.getRoles().isEmpty()) {
      for (UserRoleDomain role : domain.getRoles()) {
        role.setUserId(domain.getId());
        userRoleRepository.insert(role);
      }
    }
    return count;
  }

  @Override
  public int update(UserDomain domain) {
    domain.setLastModifiedOn(LocalDateTime.now());
    return mapper.update(converter.toEntity(domain));
  }

  @Override
  public int delete(UserDomain domain) {
    if (domain.getDeletedOn() == null) {
      domain.setDeletedOn(LocalDateTime.now());
    }
    int count = mapper.delete(converter.toEntity(domain));
    userRoleRepository.deleteByUserId(domain.getId());
    return count;
  }

  @Override
  public Optional<UserDomain> findById(String id) {
    UserDomain domain = Optional.ofNullable(mapper.findById(id)).map(converter::toDomain).orElse(null);
    if (domain != null) {
      domain.setRoles(userRoleRepository.findByUserId(domain.getId()));
      return Optional.of(domain);
    }
    return Optional.empty();
  }

  @Override
  public Optional<UserDomain> findByEmail(String email) {
    UserDomain domain = Optional.ofNullable(mapper.findByEmail(email)).map(converter::toDomain).orElse(null);
    if (domain != null) {
      domain.setRoles(userRoleRepository.findByUserId(domain.getId()));
      return Optional.of(domain);
    }
    return Optional.empty();
  }

  @Override
  public Optional<UserDomain> findByPhone(String phone) {
    UserDomain domain = Optional.ofNullable(mapper.findByPhone(phone)).map(converter::toDomain).orElse(null);
    if (domain != null) {
      domain.setRoles(userRoleRepository.findByUserId(domain.getId()));
      return Optional.of(domain);
    }
    return Optional.empty();
  }

  @Override
  public Optional<UserDomain> findOne(UserFilter filter) {
    filter.setPageSize(1);
    filter.setPageNumber(1);
    List<UserDomain> list = findAll(filter);
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }

  @Override
  public List<UserDomain> findAll(UserFilter filter) {
    List<UserDomain> domains = converter.toDomainList(mapper.findAllWithoutPagination(filter));
    enrichWithRoles(domains);
    return domains;
  }

  @Override
  public Page<UserDomain> findAllWithPagination(UserFilter filter) {
    List<UserDomain> data = converter.toDomainList(mapper.findAll(filter));
    enrichWithRoles(data);
    int total = mapper.countAll(filter);
    return new Page<>(total, filter.getPageSize(), filter.getPageNumber(), data);
  }

  private void enrichWithRoles(List<UserDomain> users) {
    if (users == null || users.isEmpty()) {
      return;
    }
    List<String> ids = users.stream().map(UserDomain::getId).toList();
    List<UserRoleDomain> roles = userRoleRepository.findAllByUserIdIn(ids);
    Map<String, List<UserRoleDomain>> rolesByUserId =
        roles.stream().collect(Collectors.groupingBy(UserRoleDomain::getUserId));

    for (UserDomain user : users) {
      user.setRoles(rolesByUserId.getOrDefault(user.getId(), new ArrayList<>()));
    }
  }
}
