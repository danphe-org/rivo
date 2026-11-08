package com.danphe.rivo.service.api;

import com.danphe.rivo.common.category.Category;
import com.danphe.rivo.common.category.CategoryDomain;
import com.danphe.rivo.common.masterdataitem.MasterDataItemDomain;
import java.util.List;

public interface CategoryService {
  List<Category> getAll();
  Category getByCode(String code);
  List<MasterDataItemDomain> getItemsByCategory(String categoryCode);
  List<CategoryDomain> getCategoriesFromMasterData(boolean includeItems);
  CategoryDomain getCategoryFromMasterDataByCode(String code, boolean includeItems);
}
