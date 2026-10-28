package com.danphe.rivo.common.library.base.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends AppException {

  public UnauthorizedException(String message) {
    super(String.valueOf(HttpStatus.UNAUTHORIZED.value()), message);
  }
}
