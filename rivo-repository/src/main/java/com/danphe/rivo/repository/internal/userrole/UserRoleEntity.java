package com.danphe.rivo.repository.internal.userrole;

import com.danphe.rivo.common.library.base.dao.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserRoleEntity extends AbstractEntity {
  private String userId;
  private String role;
}
