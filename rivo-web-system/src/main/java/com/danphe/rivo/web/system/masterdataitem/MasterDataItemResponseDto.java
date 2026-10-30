package com.danphe.rivo.web.system.masterdataitem;

import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MasterDataItemResponseDto(
    String id,
    @JsonProperty("master_data_id")
    String masterDataId,
    String title,
    String code,
    String description,
    @JsonProperty("created_on")
    LocalDateTime createdOn,
    @JsonProperty("modified_on")
    LocalDateTime modifiedOn,
    @JsonProperty("created_by")
    String createdBy,
    @JsonProperty("modified_by")
    String modifiedBy,
    boolean active,
    boolean deleted
) {
  public static MasterDataItemResponseDto from(MasterDataItemDomain domain) {
    if (domain == null) return null;
    return new MasterDataItemResponseDto(
        domain.getId(),
        domain.getMasterDataId(),
        domain.getTitle(),
        domain.getCode(),
        domain.getDescription(),
        domain.getCreatedOn(),
        domain.getLastModifiedOn(),
        domain.getCreatedBy(),
        domain.getLastModifiedBy(),
        domain.isActive(),
        domain.isDeleted()
    );
  }
}
