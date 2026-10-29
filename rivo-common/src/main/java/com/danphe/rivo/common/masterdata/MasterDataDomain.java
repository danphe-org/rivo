package com.danphe.rivo.common.masterdata;

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
public class MasterDataDomain extends AbstractDomain {

  private String title;
  private String code;
}
