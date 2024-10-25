package com.heartsave.todaktodak_api.common.exception.errorspec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorSpec implements ErrorSpec {
  INCORRECT_USERNAME_PASSWORD(
      HttpStatus.UNAUTHORIZED, "AUTH-001", "아이디 또는 비밀번호가 틀렸습니다.", "로그인이 실패됐습니다.");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
