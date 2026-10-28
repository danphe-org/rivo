package com.danphe.rivo.common.library.base.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends AppException {

  public NotFoundException(String message) {
    super(String.valueOf(HttpStatus.NOT_FOUND.value()), message);
  }

  public NotFoundException(String code, String message) {
    super(code, message);
  }
}
