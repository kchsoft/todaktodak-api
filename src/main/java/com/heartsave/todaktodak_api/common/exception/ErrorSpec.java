package com.heartsave.todaktodak_api.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorSpec {
  DIARY_DAILY_WRITING_LIMIT_EXCEPTION(HttpStatus.BAD_REQUEST, "하루 일기 작성량을 초과하였습니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않습니다.");

  private final HttpStatus status;
  private final String message;
}
