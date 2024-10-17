package com.heartsave.todaktodak_api.common.exception;

import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // API Exception
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
    return ResponseEntity.status(e.getErrorSpec().getStatus())
        .body(ErrorResponse.from(e.getErrorSpec()));
  }

  // 유효성 검사 실패
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleInvalidExceptions(MethodArgumentNotValidException e) {
    var errors = extractValidationErrors(e);
    logger.info("PARSED ERROR: {}", errors);
    return ResponseEntity.badRequest().body(ErrorResponse.from(errors));
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
