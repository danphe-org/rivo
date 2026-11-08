package com.danphe.rivo.common.category;

import com.danphe.rivo.common.library.base.service.api.AbstractDomain;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import java.util.List;
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
public class CategoryDomain extends AbstractDomain {
  private String code;
  private String name;
  private String slug;
  private String description;
  private String masterDataCode;
  private List<MasterDataItemDomain> items;
}
