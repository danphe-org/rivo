package com.danphe.rivo.repository.internal.systemuser;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.common.user.UserDomain;
import com.danphe.rivo.common.user.UserFilter;
import com.danphe.rivo.common.user.UserRoleDomain;
import com.danphe.rivo.repository.api.SystemUserRepository;
import com.danphe.rivo.repository.internal.userrole.UserRoleEntity;
import com.danphe.rivo.repository.internal.userrole.UserRoleEntityConverter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository("systemUserRepository")
public class SystemUserRepositoryImpl implements SystemUserRepository {

  private final SystemUserMapper mapper;
  private final SystemUserEntityConverter converter;
  private final UserRoleEntityConverter roleConverter;

  public SystemUserRepositoryImpl(
      SystemUserMapper mapper,
      SystemUserEntityConverter converter,
      UserRoleEntityConverter roleConverter) {
    this.mapper = mapper;
    this.converter = converter;
    this.roleConverter = roleConverter;
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
        if (role.getId() == null || role.getId().isBlank()) {
          role.setId(StringUtils.generateUUID());
        }
        if (role.getCreatedOn() == null) {
          role.setCreatedOn(LocalDateTime.now());
        }
        if (role.getLastModifiedOn() == null) {
          role.setLastModifiedOn(LocalDateTime.now());
        }
        mapper.insertRole(roleConverter.toEntity(role));
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
    mapper.deleteRolesByUserId(domain.getId());
    return count;
  }

  @Override
  public Optional<UserDomain> findById(String id) {
    UserDomain domain = Optional.ofNullable(mapper.findById(id)).map(converter::toDomain).orElse(null);
    if (domain != null) {
      domain.setRoles(roleConverter.toDomainList(mapper.findRolesByUserId(domain.getId())));
      return Optional.of(domain);
    }
    return Optional.empty();
  }

  @Override
  public Optional<UserDomain> findByEmail(String email) {
    UserDomain domain = Optional.ofNullable(mapper.findByEmail(email)).map(converter::toDomain).orElse(null);
    if (domain != null) {
      domain.setRoles(roleConverter.toDomainList(mapper.findRolesByUserId(domain.getId())));
      return Optional.of(domain);
    }
    return Optional.empty();
  }

  @Override
  public Optional<UserDomain> findByPhone(String phone) {
    UserDomain domain = Optional.ofNullable(mapper.findByPhone(phone)).map(converter::toDomain).orElse(null);
    if (domain != null) {
      domain.setRoles(roleConverter.toDomainList(mapper.findRolesByUserId(domain.getId())));
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
    List<UserRoleEntity> entities = mapper.findAllRolesByUserIds(ids);
    List<UserRoleDomain> allRoles = roleConverter.toDomainList(entities);
    Map<String, List<UserRoleDomain>> grouped = allRoles.stream()
        .collect(Collectors.groupingBy(UserRoleDomain::getUserId));
    for (UserDomain user : users) {
      user.setRoles(grouped.getOrDefault(user.getId(), new ArrayList<>()));
    }
  }
}
