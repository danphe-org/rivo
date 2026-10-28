package com.danphe.rivo.repository.internal.auditlog;

import com.danphe.rivo.common.audits.AuditDomain;
import com.danphe.rivo.common.audits.AuditFilter;
import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.util.string.StringUtils;
import com.danphe.rivo.repository.api.AuditDao;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AuditDaoImpl implements AuditDao {

  private final AuditLogsMapper mapper;
  private final AuditLogConvertor convertor;

  public AuditDaoImpl(AuditLogsMapper mapper, AuditLogConvertor convertor) {
    this.mapper = mapper;
    this.convertor = convertor;
  }

  @Override
  public int insert(AuditDomain domain) {
    if (domain.getId() == null || domain.getId().isBlank()) {
      domain.setId(StringUtils.generateUUID());
    }
    return mapper.insert(convertor.toEntity(domain));
  }

  @Override
  public int insertBulk(List<AuditDomain> domains) {
    if (domains == null || domains.isEmpty()) {
      return 0;
    }
    return mapper.insertBulk(domains.stream().map(convertor::toEntity).toList());
  }

  @Override
  public int update(AuditDomain domain) {
    return 0;
  }

  @Override
  public int delete(AuditDomain domain) {
    return 0;
  }

  @Override
  public Page<AuditDomain> findAllWithPagination(AuditFilter filter) {
    return null;
  }

  @Override
  public List<AuditDomain> findAll(AuditFilter filter) {
    return List.of();
  }

  @Override
  public Optional<AuditDomain> findOne(AuditFilter filter) {
    return Optional.empty();
  }

  @Override
  public Optional<AuditDomain> findById(String id) {
    return Optional.empty();
  }
}
