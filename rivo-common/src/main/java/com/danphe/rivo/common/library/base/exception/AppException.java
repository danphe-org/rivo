package com.danphe.rivo.common.library.base.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
  protected final ExceptionType exceptionType;

  public AppException(ExceptionType exceptionType) {
    super(exceptionType.getDescription());
    this.exceptionType = exceptionType;
  }

  public AppException(String code, String message) {
    super(message);
    this.exceptionType =
        new ExceptionType() {
          @Override
          public String getCode() {
            return code;
          }

          @Override
          public String getDescription() {
            return message;
          }
        };
  }

  public AppException(String message) {
    super(message);
    this.exceptionType =
        new ExceptionType() {
          @Override
          public String getCode() {
            return String.valueOf(HttpStatus.BAD_REQUEST.value());
          }

          @Override
          public String getDescription() {
            return message;
          }
        };
  }
}
