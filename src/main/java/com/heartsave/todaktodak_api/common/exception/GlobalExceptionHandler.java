package com.heartsave.todaktodak_api.common.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // API Exception
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
    log.error(e.getLogMessage());
    return ResponseEntity.status(e.getErrorSpec().getStatus())
        .body(ErrorResponse.from(e.getErrorSpec()));
  }

  // 유효성 검사 실패
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleInvalidException(MethodArgumentNotValidException e) {
    var errors = extractValidationErrors(e);
    logger.error("유효성 검사 실패 - 무결성: {}", errors);
    return ResponseEntity.badRequest().body(ErrorResponse.from(errors));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    var errors = extractMisMatchErrors(e);
    logger.error("유효성 검사 실패 - 타입: {}", errors);
    return ResponseEntity.badRequest().body(ErrorResponse.from(errors));
  }

  private Map<String, String> extractMisMatchErrors(MethodArgumentTypeMismatchException e) {
    Map<String, String> errors = new HashMap<>();
    errors.put(e.getName(), Objects.requireNonNull(e.getValue()).toString());
    return errors;
  }

  private Map<String, String> extractValidationErrors(BindException e) {
    return e.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(this::getFieldName, this::getFieldMessage));
  }

  private String getFieldName(FieldError fe) {
    return fe.getField();
  }

  private String getFieldMessage(FieldError fe) {
    return fe.getDefaultMessage();
  }
}
