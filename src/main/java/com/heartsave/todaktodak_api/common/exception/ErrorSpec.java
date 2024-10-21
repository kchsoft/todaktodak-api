package com.heartsave.todaktodak_api.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorSpec {
  NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않습니다.");

  private final HttpStatus status;
  private final String message;
}
