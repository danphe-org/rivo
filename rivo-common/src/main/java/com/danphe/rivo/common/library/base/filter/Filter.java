package com.danphe.rivo.common.library.base.filter;

import java.util.List;

public interface Filter {

  String getId();

  void setId(String id);

  List<String> getIds();

  void setIds(List<String> ids);

  int getPageSize();

  void setPageSize(int pageSize);

  int getPageNumber();

  void setPageNumber(int pageNumber);

  String getSortField();

  void setSortField(String sortField);

  String getSortOrder();

  void setSortOrder(String sortOrder);

  String getSortColumn();

  String getSortDirection();

  Boolean getActive();

  void setActive(Boolean active);

  String getSearchParameter();

  void setSearchParameter(String searchParameter);

  Boolean getDelete();

  void setDelete(Boolean delete);
}
