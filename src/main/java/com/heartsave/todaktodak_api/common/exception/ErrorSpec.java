package com.heartsave.todaktodak_api.common.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorSpec {
  LIMIT_EXCEED(HttpStatus.BAD_REQUEST, "최대 횟수를 초과했습니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않습니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
  INCORRECT_USERNAME_PASSWORD(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 틀렸습니다.");

  private final HttpStatus status;
  private final String description;
}
