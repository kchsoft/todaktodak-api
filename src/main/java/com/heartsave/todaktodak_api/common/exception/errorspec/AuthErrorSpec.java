package com.heartsave.todaktodak_api.common.exception.errorspec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorSpec implements ErrorSpec {
  INCORRECT_USERNAME_PASSWORD(
      HttpStatus.UNAUTHORIZED, "AUTH-001", "아이디 또는 비밀번호가 틀렸습니다.", "로그인이 실패됐습니다."),
  DUPLICATED_INFORMATION(HttpStatus.CONFLICT, "AUTH-002", "중복된 정보가 있습니다.", "회원가입 요청 실패"),
  OAUTH_LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "AUTH-003", "소셜 로그인이 실패됐습니다.", "OAuth2 로그인 실패");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
