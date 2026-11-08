package com.danphe.rivo.service.internal.category;

import com.danphe.rivo.common.category.Category;
import com.danphe.rivo.common.category.CategoryDomain;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.repository.api.MasterDataItemRepository;
import com.danphe.rivo.service.api.CategoryService;
import com.danphe.rivo.service.internal.category.usecase.CategoryGetAllUseCase;
import com.danphe.rivo.service.internal.category.usecase.CategoryGetByCodeUseCase;
import com.danphe.rivo.service.internal.matchrequest.validator.MatchRequestMasterDataValidator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

  private final MasterDataItemRepository masterDataItemRepository;
  private final MatchRequestMasterDataValidator masterDataValidator;

  public CategoryServiceImpl(
      MasterDataItemRepository masterDataItemRepository,
      MatchRequestMasterDataValidator masterDataValidator) {
    this.masterDataItemRepository = masterDataItemRepository;
    this.masterDataValidator = masterDataValidator;
  }

  @Override
  public List<Category> getAll() {
    return new CategoryGetAllUseCase().execute(null);
  }

  @Override
  public Category getByCode(String code) {
    return new CategoryGetByCodeUseCase().execute(code);
  }

  @Override
  public List<MasterDataItemDomain> getItemsByCategory(String categoryCode) {
    return masterDataValidator.resolveGamesForCategory(categoryCode);
  }

  @Override
  public List<CategoryDomain> getCategoriesFromMasterData(boolean includeItems) {
    List<MasterDataItemDomain> dbItems = masterDataItemRepository.findByMasterDataCode("CATEGORY");
    if (dbItems != null && !dbItems.isEmpty()) {
      List<CategoryDomain> domains = new ArrayList<>();
      for (MasterDataItemDomain item : dbItems) {
        String code = item.getCode() != null && !item.getCode().isBlank()
            ? item.getCode().toUpperCase()
            : item.getTitle().toUpperCase().replace(" ", "_");
        Category enumCat = Category.fromString(code);
        String masterDataCode = enumCat != null && enumCat.getMasterDataCode() != null
            ? enumCat.getMasterDataCode()
            : code + "_GAMES";
        List<MasterDataItemDomain> games = includeItems
            ? masterDataValidator.resolveGamesForCategory(code)
            : null;

        CategoryDomain domain = CategoryDomain.builder()
            .code(code)
            .name(item.getTitle())
            .slug(item.getTitle().toLowerCase().replace(" ", "-"))
            .description(item.getDescription())
            .masterDataCode(masterDataCode)
            .items(games)
            .build();
        domain.setId(item.getId());
        domains.add(domain);
      }
      return domains;
    }

    return Arrays.stream(Category.values())
        .map(cat -> {
          List<MasterDataItemDomain> games = includeItems
              ? masterDataValidator.resolveGamesForCategory(cat.getCode())
              : null;
          return CategoryDomain.builder()
              .code(cat.getCode())
              .name(cat.getName())
              .slug(cat.getSlug())
              .description(cat.getDescription())
              .masterDataCode(cat.getMasterDataCode())
              .items(games)
              .build();
        })
        .toList();
  }

  @Override
  public CategoryDomain getCategoryFromMasterDataByCode(String code, boolean includeItems) {
    if (code == null || code.isBlank()) {
      throw new NotFoundException("Category code is required");
    }
    String trimmed = code.trim();
    List<CategoryDomain> all = getCategoriesFromMasterData(includeItems);
    return all.stream()
        .filter(c -> c.getCode().equalsIgnoreCase(trimmed)
            || c.getName().equalsIgnoreCase(trimmed)
            || (c.getSlug() != null && c.getSlug().equalsIgnoreCase(trimmed))
            || (c.getId() != null && c.getId().equalsIgnoreCase(trimmed)))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Category not found with code: " + code));
  }
}
