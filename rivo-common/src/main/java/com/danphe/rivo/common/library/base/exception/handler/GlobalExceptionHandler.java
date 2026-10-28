package com.danphe.rivo.common.library.base.exception.handler;

import com.danphe.rivo.common.library.base.exception.AppException;
import com.danphe.rivo.common.library.base.exception.ForbiddenException;
import com.danphe.rivo.common.library.base.exception.NotFoundException;
import com.danphe.rivo.common.library.base.exception.UnauthorizedException;
import com.danphe.rivo.common.library.base.rest.ResponseTemplate;
import com.danphe.rivo.common.library.base.rest.RestResponse;
import com.danphe.rivo.common.library.util.date.DateUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  public ResponseEntity<ResponseTemplate<String>> handleUnexpectedException(Exception e) {
    log.error("Unhandled exception", e);
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    ResponseTemplate<String> body = new ResponseTemplate<>();
    body.setCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    body.setMessage(e.getMessage());
    body.setData(sw.toString());
    body.setTime(DateUtils.now());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
