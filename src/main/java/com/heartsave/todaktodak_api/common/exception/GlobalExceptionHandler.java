package com.heartsave.todaktodak_api.common.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // 유효성 검사 실패
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleInvalidExceptions(MethodArgumentNotValidException e) {
    var errors = parseError(e);
    logger.info("PARSED ERROR: {}", errors);
    return ResponseEntity.badRequest().body(ErrorResponse.from(errors));
  }

  private Map<String, String> parseError(BindException e) {
    Map<String, String> fieldErrors = new HashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(
            (fe) ->
                fieldErrors.put(
                    fe.getField(), Optional.ofNullable(fe.getDefaultMessage()).orElse("")));
    return fieldErrors;
  }
}
