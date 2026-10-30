package com.danphe.rivo.repository.internal.masterdataitem;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.common.masterdataitem.MasterDataItemFilter;
import com.danphe.rivo.repository.api.MasterDataItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MasterDataItemRepositoryImpl implements MasterDataItemRepository {

  private final MasterDataItemMapper mapper;
  private final MasterDataItemEntityConverter converter;

  public MasterDataItemRepositoryImpl(
      MasterDataItemMapper mapper,
      MasterDataItemEntityConverter converter) {
    this.mapper = mapper;
    this.converter = converter;
  }

  @Override
  public int insert(MasterDataItemDomain domain) {
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
  public int update(MasterDataItemDomain domain) {
    domain.setLastModifiedOn(LocalDateTime.now());
    return mapper.update(converter.toEntity(domain));
  }

  @Override
  public int delete(MasterDataItemDomain domain) {
    if (domain.getDeletedOn() == null) {
      domain.setDeletedOn(LocalDateTime.now());
    }
    return mapper.delete(converter.toEntity(domain));
  }

  @Override
  public Optional<MasterDataItemDomain> findById(String id) {
    return Optional.ofNullable(mapper.findById(id)).map(converter::toDomain);
  }

  @Override
  public List<MasterDataItemDomain> findByMasterDataId(String masterDataId) {
    return converter.toDomainList(mapper.findByMasterDataId(masterDataId));
  }

  @Override
  public Optional<MasterDataItemDomain> findByMasterDataIdAndTitle(String masterDataId, String title) {
    return Optional.ofNullable(mapper.findByMasterDataIdAndTitle(masterDataId, title)).map(converter::toDomain);
  }

  @Override
  public Optional<MasterDataItemDomain> findOne(MasterDataItemFilter filter) {
    filter.setPageSize(1);
    filter.setPageNumber(1);
    List<MasterDataItemDomain> list = findAll(filter);
    return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
  }

  @Override
  public List<MasterDataItemDomain> findAll(MasterDataItemFilter filter) {
    return converter.toDomainList(mapper.findAllWithoutPagination(filter));
  }

  @Override
  public Page<MasterDataItemDomain> findAllWithPagination(MasterDataItemFilter filter) {
    List<MasterDataItemDomain> data = converter.toDomainList(mapper.findAll(filter));
    int total = mapper.countAll(filter);
    return new Page<>(total, filter.getPageSize(), filter.getPageNumber(), data);
  }
}
