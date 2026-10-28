package com.danphe.rivo.common.library.base.common;

import java.util.List;
import java.util.function.Function;

public record Page<T>(int totalRecords, int limit, int currentPage, List<T> data) {

  public <R> Page<R> map(Function<T, R> mapper) {
    return new Page<>(totalRecords, limit, currentPage, data.stream().map(mapper).toList());
  }
}
