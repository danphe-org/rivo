package com.danphe.rivo.web.system.category;

import com.danphe.rivo.common.category.CategoryDomain;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.service.api.CategoryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/categories")
@PreAuthorize("hasAnyRole('SYSTEM_USER', 'ADMIN', 'STAFF') or hasAuthority('SYSTEM_USER')")
public class CategoryController {

  private final CategoryService service;

  public CategoryController(CategoryService service) {
    this.service = service;
  }

  @GetMapping
  public ResponseEntity<?> getAll() {
    List<CategoryDomain> categories = service.getCategoriesFromMasterData(false);
    List<CategoryResponseDto> response = categories.stream()
        .map(CategoryResponseDto::fromDomain)
        .toList();
    return RestResponse.ok(response);
  }

  @GetMapping("/{code}")
  public ResponseEntity<?> getByCode(@PathVariable String code) {
    CategoryDomain category = service.getCategoryFromMasterDataByCode(code, false);
    return RestResponse.ok(CategoryResponseDto.fromDomain(category));
  }
}
