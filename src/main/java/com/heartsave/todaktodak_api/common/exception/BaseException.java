package com.heartsave.todaktodak_api.common.exception;

import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;
import java.util.StringJoiner;
import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
  private final ErrorSpec errorSpec;
  private final ErrorField errorField;

  protected BaseException(ErrorSpec errorSpec) {
    this.errorSpec = errorSpec;
    this.errorField = new ErrorField();
  }

  public String getLogMessage() {
    StringJoiner joiner = new StringJoiner(", ", "[ ", " ]");
    errorField.getBy().forEach((key, value) -> joiner.add(key + "=" + value));
    return String.format(
        "[ERROR] %s : %s = %s %s",
        errorSpec.getCode(), errorSpec.name(), errorSpec.getDebugMessage(), joiner);
  }
}
