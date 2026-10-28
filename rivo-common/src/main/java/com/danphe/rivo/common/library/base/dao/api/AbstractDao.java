package com.danphe.rivo.common.library.base.dao.api;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.filter.Filter;
import com.danphe.rivo.common.library.base.service.api.Domain;
import java.util.List;
import java.util.Optional;

public interface AbstractDao<D extends Domain, F extends Filter> extends BaseDao {
  int insert(D domain);

  int update(D domain);

  int delete(D filter);

  Page<D> findAllWithPagination(F filter);

  List<D> findAll(F filter);

  Optional<D> findOne(F filter);

  Optional<D> findById(String id);
}
