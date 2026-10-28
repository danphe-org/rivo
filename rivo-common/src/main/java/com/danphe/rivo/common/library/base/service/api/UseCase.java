package com.danphe.rivo.common.library.base.service.api;

public interface UseCase<Rq, Rp> {
  Rp execute(Rq request);
}
