package com.danphe.rivo.common.library.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenException extends AppException {

  public ForbiddenException(String message) {
    super(String.valueOf(HttpStatus.FORBIDDEN.value()), message);
  }

  @AllArgsConstructor
  @Getter
  public enum ForbiddenError implements ExceptionType {
    NOT_AUTHORIZED("403", "Not Authorized");
    private final String code;
    private final String description;
  }
}
