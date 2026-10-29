package com.danphe.rivo.web.system.masterdata;

import com.danphe.rivo.common.masterdata.MasterDataDomain;
import jakarta.validation.constraints.Size;

public record MasterDataUpdateDto(
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    String title,

    @Size(max = 100, message = "Code cannot exceed 100 characters")
    String code
) {
  public MasterDataDomain toDomain(String id) {
    MasterDataDomain domain = new MasterDataDomain();
    domain.setId(id);
    domain.setTitle(title);
    domain.setCode(code);
    return domain;
  }
}
