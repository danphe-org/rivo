package com.danphe.rivo.service.internal.category.usecase;

import com.danphe.rivo.common.category.Category;
import com.danphe.rivo.common.library.base.service.api.UseCase;
import java.util.Arrays;
import java.util.List;

public class CategoryGetAllUseCase implements UseCase<Void, List<Category>> {

  @Override
  public List<Category> execute(Void request) {
    return Arrays.asList(Category.values());
  }
}
