package com.danphe.rivo.repository.api;

import com.danphe.rivo.common.audits.AuditDomain;
import com.danphe.rivo.common.audits.AuditFilter;
import com.danphe.rivo.common.library.base.dao.api.AbstractDao;
import java.util.List;

public interface AuditDao extends AbstractDao<AuditDomain, AuditFilter> {
  int insertBulk(List<AuditDomain> domains);
}
