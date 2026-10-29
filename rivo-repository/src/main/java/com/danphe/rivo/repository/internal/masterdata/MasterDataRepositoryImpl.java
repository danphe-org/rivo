package com.danphe.rivo.repository.internal.masterdata;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.danphe.rivo.common.masterdata.MasterDataFilter;
import com.danphe.rivo.repository.api.MasterDataRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MasterDataRepositoryImpl implements MasterDataRepository {

  private final MasterDataMapper mapper;
  private final MasterDataEntityConverter converter;

  public MasterDataRepositoryImpl(
      MasterDataMapper mapper,
      MasterDataEntityConverter converter) {
    this.mapper = mapper;
    this.converter = converter;
  }

  @Override
  public int insert(MasterDataDomain domain) {
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
  public int update(MasterDataDomain domain) {
    domain.setLastModifiedOn(LocalDateTime.now());
    return mapper.update(converter.toEntity(domain));
  }

  @Override
  public int delete(MasterDataDomain domain) {
    if (domain.getDeletedOn() == null) {
      domain.setDeletedOn(LocalDateTime.now());
    }
    return mapper.delete(converter.toEntity(domain));
  }

  @Override
  public Optional<MasterDataDomain> findById(String id) {
    return Optional.ofNullable(mapper.findById(id)).map(converter::toDomain);
  }

  @Override
  public List<MasterDataDomain> findByCode(String code) {
    return converter.toDomainList(mapper.findByCode(code));
  }

  @Override
  public Optional<MasterDataDomain> findByCodeAndTitle(String code, String title) {
    return Optional.ofNullable(mapper.findByCodeAndTitle(code, title)).map(converter::toDomain);
  }

  @Override
  public Optional<MasterDataDomain> findOne(MasterDataFilter filter) {
    filter.setPageSize(1);
    filter.setPageNumber(1);
    List<MasterDataDomain> list = findAll(filter);
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }

  @Override
  public List<MasterDataDomain> findAll(MasterDataFilter filter) {
    return converter.toDomainList(mapper.findAllWithoutPagination(filter));
  }

  @Override
  public Page<MasterDataDomain> findAllWithPagination(MasterDataFilter filter) {
    List<MasterDataDomain> data = converter.toDomainList(mapper.findAll(filter));
    int total = mapper.countAll(filter);
    return new Page<>(total, filter.getPageSize(), filter.getPageNumber(), data);
  }
}
