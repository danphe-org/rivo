package com.danphe.rivo.common.library.base.service.api;

import com.danphe.rivo.common.library.base.common.Page;
import com.danphe.rivo.common.library.base.filter.Filter;

public interface SearchUseCase<F extends Filter, D extends Domain> {
  Page<D> execute(F filter);
}
