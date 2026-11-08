package com.danphe.rivo.service.internal.category.usecase;

import com.danphe.rivo.common.category.Category;
import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.library.base.service.api.UseCase;

public class CategoryGetByCodeUseCase implements UseCase<String, Category> {

  @Override
  public Category execute(String code) {
    validate(code);
    Category category = Category.fromString(code);
    if (category == null) {
      throw new NotFoundException("Category not found with code: " + code);
    }
    return category;
  }

  private void validate(String code) {
    if (code == null || code.isBlank()) {
      throw new AppException("Category code is required");
    }
  }
}
