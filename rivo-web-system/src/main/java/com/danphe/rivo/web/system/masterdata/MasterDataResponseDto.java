package com.danphe.rivo.web.system.masterdata;

import com.danphe.rivo.common.masterdata.MasterDataDomain;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MasterDataResponseDto(
    String id,
    String title,
    String code,
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
  public static MasterDataResponseDto from(MasterDataDomain domain) {
    if (domain == null) return null;
    return new MasterDataResponseDto(
        domain.getId(),
        domain.getTitle(),
        domain.getCode(),
        domain.getCreatedOn(),
        domain.getLastModifiedOn(),
        domain.getCreatedBy(),
        domain.getLastModifiedBy(),
        domain.isActive(),
        domain.isDeleted()
    );
  }
}
