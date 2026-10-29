package com.danphe.rivo.web.system.masterdata;

import com.danphe.rivo.common.masterdata.MasterDataDomain;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MasterDataCreateDto(
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    String title,

    @NotBlank(message = "Code is required")
    @Size(max = 100, message = "Code cannot exceed 100 characters")
    String code
) {
  public MasterDataDomain toDomain() {
    MasterDataDomain domain = new MasterDataDomain();
    domain.setTitle(title);
    domain.setCode(code);
    return domain;
  }
}
