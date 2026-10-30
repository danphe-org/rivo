package com.danphe.rivo.repository.internal.masterdataitem;

import com.danphe.rivo.common.library.base.dao.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MasterDataItemEntity extends AbstractEntity {

  private String masterDataId;
  private String title;
  private String code;
  private String description;
}
