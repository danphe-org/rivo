package com.danphe.rivo.repository.internal.matchrequest;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.common.matchrequest.MatchRequestDomain;
import com.danphe.rivo.common.matchrequest.MatchRequestFilter;
import com.danphe.rivo.repository.api.MatchRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MatchRequestRepositoryImpl implements MatchRequestRepository {

  private final MatchRequestMapper mapper;
  private final MatchRequestEntityConverter converter;

  public MatchRequestRepositoryImpl(
      MatchRequestMapper mapper,
      MatchRequestEntityConverter converter) {
    this.mapper = mapper;
    this.converter = converter;
  }

  @Override
  public int insert(MatchRequestDomain domain) {
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
  public int update(MatchRequestDomain domain) {
    domain.setLastModifiedOn(LocalDateTime.now());
    return mapper.update(converter.toEntity(domain));
  }

  @Override
  public int delete(MatchRequestDomain domain) {
    if (domain.getDeletedOn() == null) {
      domain.setDeletedOn(LocalDateTime.now());
    }
    return mapper.delete(converter.toEntity(domain));
  }

  @Override
  public Optional<MatchRequestDomain> findById(String id) {
    return Optional.ofNullable(mapper.findById(id)).map(converter::toDomain);
  }

  @Override
  public Optional<MatchRequestDomain> findOne(MatchRequestFilter filter) {
    filter.setPageSize(1);
    filter.setPageNumber(1);
    List<MatchRequestDomain> list = findAll(filter);
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }

  @Override
  public List<MatchRequestDomain> findAll(MatchRequestFilter filter) {
    return converter.toDomainList(mapper.findAllWithoutPagination(filter));
  }

  @Override
  public Page<MatchRequestDomain> findAllWithPagination(MatchRequestFilter filter) {
    List<MatchRequestDomain> data = converter.toDomainList(mapper.findAll(filter));
    int total = mapper.countAll(filter);
    return new Page<>(total, filter.getPageSize(), filter.getPageNumber(), data);
  }
}
