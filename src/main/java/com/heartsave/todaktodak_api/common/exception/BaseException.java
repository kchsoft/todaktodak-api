package com.heartsave.todaktodak_api.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseException extends RuntimeException {
  private final ErrorSpec errorSpec;
  private final String clientMessage;
  private final String logMessage;
}
