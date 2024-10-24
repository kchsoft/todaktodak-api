package com.heartsave.todaktodak_api.common.exception.errorspec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TokenErrorSpec implements ErrorSpec {
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-001", "로그인 정보가 만료되었습니다.", "회원의 토큰이 만료되었습니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-002", "유효하지 않은 로그인 정보 입니다.", "회원의 토큰이 유효하지 않습니다.");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
