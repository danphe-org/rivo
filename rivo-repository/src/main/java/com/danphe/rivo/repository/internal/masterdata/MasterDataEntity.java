package com.danphe.rivo.repository.internal.masterdata;

import com.danphe.rivo.common.library.base.dao.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MasterDataEntity extends AbstractEntity {

  private String title;
  private String code;
}
