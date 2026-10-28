package com.danphe.rivo.repository.internal.auditlog;

import com.danphe.rivo.common.audits.AuditEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogsMapper {
  int insert(AuditEntity entity);

  int insertBulk(List<AuditEntity> entities);
}
