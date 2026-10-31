package com.danphe.rivo.repository.internal.clientuser;

import com.danphe.rivo.common.library.base.dao.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClientUserEntity extends AbstractEntity {
  private String name;
  private String email;
  private String password;
  private String phone;
  private Boolean verified;
}
