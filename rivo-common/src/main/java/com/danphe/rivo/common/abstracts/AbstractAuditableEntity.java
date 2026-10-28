package com.danphe.rivo.common.abstracts;

import com.danphe.rivo.common.audits.AuditEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AbstractAuditableEntity extends AuditEntity {
  private String newEntity;
  private String oldEntity;
}
