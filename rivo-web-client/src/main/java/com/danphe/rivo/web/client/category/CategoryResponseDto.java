package com.danphe.rivo.web.client.category;

import com.danphe.rivo.common.category.Category;
import com.danphe.rivo.common.category.CategoryDomain;
import com.danphe.rivo.web.client.masterdataitem.MasterDataItemResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryResponseDto(
    String id,
    String code,
    String name,
    String slug,
    String description,
    @JsonProperty("master_data_code")
    String masterDataCode,
    List<MasterDataItemResponseDto> items
) {
  public static CategoryResponseDto from(Category category) {
    if (category == null) return null;
    return new CategoryResponseDto(
        null,
        category.getCode(),
        category.getName(),
        category.getSlug(),
        category.getDescription(),
        category.getMasterDataCode(),
        null
    );
  }

  public static CategoryResponseDto fromDomain(CategoryDomain domain) {
    if (domain == null) return null;
    List<MasterDataItemResponseDto> itemDtos = domain.getItems() != null
        ? domain.getItems().stream().map(MasterDataItemResponseDto::from).toList()
        : null;
    return new CategoryResponseDto(
        domain.getId(),
        domain.getCode(),
        domain.getName(),
        domain.getSlug(),
        domain.getDescription(),
        domain.getMasterDataCode(),
        itemDtos
    );
  }
}
