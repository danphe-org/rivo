package com.danphe.rivo.common.audits;

import com.danphe.rivo.common.library.base.filter.AbstractFilter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditFilter extends AbstractFilter {
  private String entityKey;
  private String entity;
  private String requestType;
}
