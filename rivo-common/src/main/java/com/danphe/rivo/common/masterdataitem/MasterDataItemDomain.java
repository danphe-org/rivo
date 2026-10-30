package com.danphe.rivo.common.masterdataitem;

import com.danphe.rivo.common.library.base.service.api.AbstractDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MasterDataItemDomain extends AbstractDomain {

  private String masterDataId;
  private String title;
  private String code;
  private String description;
}
