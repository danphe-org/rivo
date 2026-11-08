package com.danphe.rivo.web.system.category;

import com.danphe.rivo.common.category.Category;
import com.danphe.rivo.common.category.CategoryDomain;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryResponseDto(
    String id,
    String code,
    String name,
    String slug,
    String description,
    @JsonProperty("master_data_code")
    String masterDataCode
) {
  public static CategoryResponseDto from(Category category) {
    if (category == null) return null;
    return new CategoryResponseDto(
        null,
        category.getCode(),
        category.getName(),
        category.getSlug(),
        category.getDescription(),
        category.getMasterDataCode()
    );
  }

  public static CategoryResponseDto fromDomain(CategoryDomain domain) {
    if (domain == null) return null;
    return new CategoryResponseDto(
        domain.getId(),
        domain.getCode(),
        domain.getName(),
        domain.getSlug(),
        domain.getDescription(),
        domain.getMasterDataCode()
    );
  }
}
