package com.heartsave.todaktodak_api.common.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorFieldBuilder {
  private final ErrorField errorField;

  public static ErrorFieldBuilder builder() {
    return new ErrorFieldBuilder(new ErrorField());
  }

  public ErrorFieldBuilder add(String key, Object value) {
    errorField.add(key, value);
    return this;
  }

  public ErrorField build() {
    return errorField;
  }
}
