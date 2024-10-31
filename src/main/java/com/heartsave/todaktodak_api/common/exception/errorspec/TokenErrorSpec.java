package com.heartsave.todaktodak_api.common.exception.errorspec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TokenErrorSpec implements ErrorSpec {
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-001", "로그인 정보가 만료되었습니다.", "액세스 토큰이 만료되었습니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-002", "비정상 로그인 시도입니다.", "유효하지 않은 토큰으로 인증 시도"),
  NON_EXISTENT_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN-003", "로그인 정보가 만료되었습니다.", "액세스 토큰이 없는 요청입니다.");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
