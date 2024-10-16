package com.heartsave.todaktodak_api.common.exception;

import java.util.List;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_GATEWAY)
  public ResponseEntity<List<String>> handleInvalidException(MethodArgumentNotValidException e) {
    var errors = parseError(e);
    return ResponseEntity.badRequest().body(errors);
  }

  private List<String> parseError(BindException e) {
    return e.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .toList();
  }
}
