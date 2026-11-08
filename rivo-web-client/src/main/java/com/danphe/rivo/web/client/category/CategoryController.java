package com.danphe.rivo.web.client.category;

import com.danphe.rivo.common.category.CategoryDomain;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import com.danphe.rivo.service.api.CategoryService;
import com.danphe.rivo.web.client.masterdataitem.MasterDataItemResponseDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client/categories")
public class CategoryController {

  private final CategoryService service;

  public CategoryController(CategoryService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<?> getAll(@RequestParam(required = false, defaultValue = "false") boolean includeItems) {
    List<CategoryDomain> categories = service.getCategoriesFromMasterData(includeItems);
    List<CategoryResponseDto> response = categories.stream()
        .map(CategoryResponseDto::fromDomain)
        .toList();
    return RestResponse.ok(response);
  }

  @GetMapping("/tree")
  public ResponseEntity<?> getTree() {
    return getAll(true);
  }

  @GetMapping("/{code}")
  public ResponseEntity<?> getByCode(@PathVariable String code) {
    CategoryDomain category = service.getCategoryFromMasterDataByCode(code, false);
    return RestResponse.ok(CategoryResponseDto.fromDomain(category));
  }

  @GetMapping("/{code}/items")
  public ResponseEntity<?> getItemsByCategory(@PathVariable String code) {
    List<MasterDataItemDomain> items = service.getItemsByCategory(code);
    List<MasterDataItemResponseDto> response = items.stream()
        .map(MasterDataItemResponseDto::from)
        .toList();
    return RestResponse.ok(response);
  }
}
