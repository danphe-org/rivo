package com.danphe.rivo.web.system.masterdataitem;

import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MasterDataItemCreateDto(
    @NotBlank(message = "Master data ID is required")
    @JsonProperty("master_data_id")
    String masterDataId,

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    String title,

    @Size(max = 100, message = "Code cannot exceed 100 characters")
    String code,

    String description
) {
  public MasterDataItemDomain toDomain() {
    MasterDataItemDomain domain = new MasterDataItemDomain();
    domain.setMasterDataId(masterDataId);
    domain.setTitle(title);
    domain.setCode(code);
    domain.setDescription(description);
    return domain;
  }
}
