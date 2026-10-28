package com.danphe.rivo.repository.internal.userrole;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.common.user.UserFilter;
import com.danphe.rivo.common.user.UserRoleDomain;
import com.danphe.rivo.repository.api.UserRoleRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRoleRepositoryImpl implements UserRoleRepository {

  private final UserRoleMapper mapper;
  private final UserRoleEntityConverter converter;

  public UserRoleRepositoryImpl(UserRoleMapper mapper, UserRoleEntityConverter converter) {
    this.mapper = mapper;
    this.converter = converter;
  }

  @Override
  public int insert(UserRoleDomain domain) {
    if (domain.getId() == null || domain.getId().isBlank()) {
      domain.setId(StringUtils.generateUUID());
    }
    if (domain.getCreatedOn() == null) {
      domain.setCreatedOn(LocalDateTime.now());
    }
    if (domain.getLastModifiedOn() == null) {
      domain.setLastModifiedOn(LocalDateTime.now());
    }
    return mapper.insert(converter.toEntity(domain));
  }

  @Override
  public int update(UserRoleDomain domain) {
    domain.setLastModifiedOn(LocalDateTime.now());
    return mapper.update(converter.toEntity(domain));
  }

  @Override
  public int delete(UserRoleDomain domain) {
    if (domain.getDeletedOn() == null) {
      domain.setDeletedOn(LocalDateTime.now());
    }
    return mapper.delete(converter.toEntity(domain));
  }

  @Override
  public int deleteByUserId(String userId) {
    return mapper.deleteByUserId(userId);
  }

  @Override
  public Optional<UserRoleDomain> findById(String id) {
    return Optional.ofNullable(mapper.findById(id)).map(converter::toDomain);
  }

  @Override
  public List<UserRoleDomain> findByUserId(String userId) {
    return converter.toDomainList(mapper.findByUserId(userId));
  }

  @Override
  public List<UserRoleDomain> findAllByUserIdIn(List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return List.of();
    }
    return converter.toDomainList(mapper.findAllByUserIdIn(userIds));
  }

  @Override
  public Page<UserRoleDomain> findAllWithPagination(UserFilter filter) {
    return null;
  }

  @Override
  public List<UserRoleDomain> findAll(UserFilter filter) {
    return List.of();
  }

  @Override
  public Optional<UserRoleDomain> findOne(UserFilter filter) {
    return Optional.empty();
  }
}
