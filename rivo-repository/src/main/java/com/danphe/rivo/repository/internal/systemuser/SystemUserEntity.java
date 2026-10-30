package com.danphe.rivo.repository.internal.systemuser;

import com.danphe.rivo.common.library.base.dao.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SystemUserEntity extends AbstractEntity {
  private String name;
  private String email;
  private String password;
  private String phone;
}
