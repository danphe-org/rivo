package com.danphe.rivo.common.library.base.exception.handler;

import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.library.base.exception.UnauthorizedException;
import com.danphe.rivo.common.library.base.rest.ResponseTemplate;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.library.util.date.DateUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseTemplate<String>> handleValidationException(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return RestResponse.error(new AppException(message));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ResponseTemplate<String>> handleNotFoundException(NotFoundException e) {
    ResponseTemplate<String> body = RestResponse.error(e).getBody();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ResponseTemplate<String>> handleForbiddenException(ForbiddenException e) {
    ResponseTemplate<String> body = RestResponse.error(e).getBody();
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ResponseTemplate<String>> handleUnauthorizedException(UnauthorizedException e) {
    ResponseTemplate<String> body = RestResponse.error(e).getBody();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
  }

  @ExceptionHandler(AppException.class)
  public ResponseEntity<ResponseTemplate<String>> handleAppException(AppException e) {
    return RestResponse.error(e);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseTemplate<String>> handleUnexpectedException(
      Exception e, HttpServletRequest request) {
    Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
    String summary = cause.toString().lines().findFirst().orElse(cause.getClass().getName());
    log.error("Unhandled exception for {} {} [{}]", request.getMethod(), request.getRequestURI(), summary, e);
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    ResponseTemplate<String> body =
        new ResponseTemplate<>(String.valueOf(status.value()), status.getReasonPhrase(), null, DateUtils.now());
    return ResponseEntity.status(status).body(body);
  }
}
