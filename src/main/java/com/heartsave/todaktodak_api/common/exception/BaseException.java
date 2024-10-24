package com.heartsave.todaktodak_api.common.exception;

import java.util.StringJoiner;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
  private final ErrorSpec errorSpec;
  private final ErrorField errorField;
  private final String clientMessage;
  private final String debugMessage;

  protected BaseException(ErrorSpec errorSpec, String clientMessage, String debugMessage) {
    this.errorSpec = errorSpec;
    this.errorField = new ErrorField();
    this.clientMessage = clientMessage;
    this.debugMessage = debugMessage;
  }

  public String getLogMessage() {
    StringJoiner joiner = new StringJoiner(", ", "[ ", " ]");
    errorField.getBy().forEach((key, value) -> joiner.add(key + "=" + value));
    return String.format("%s : %s %s", errorSpec.name(), debugMessage, joiner);
  }
}
