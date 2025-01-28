package com.heartsave.todaktodak_api.common.exception.errorspec.auth;

import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorSpec implements ErrorSpec {
  INCORRECT_USERNAME_PASSWORD(
      HttpStatus.UNAUTHORIZED, "AUTH-001", "아이디 또는 비밀번호가 틀렸습니다.", "로그인이 실패됐습니다."),
  DUPLICATED_INFORMATION(HttpStatus.CONFLICT, "AUTH-002", "중복된 정보가 있습니다.", "회원가입 요청 실패"),
  OAUTH_LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "AUTH-003", "소셜 로그인이 실패됐습니다.", "OAuth2 로그인 실패"),
  OAUTH_DUPLICATED_EMAIL(HttpStatus.UNAUTHORIZED, "AUTH-004", "이미 가입된 계정입니다.", "OAuth2 중복 회원가입 시도"),
  BASE_DUPLICATED_EMAIL(HttpStatus.CONFLICT, "AUTH-005", "이미 가입된 계정입니다.", "기본 중복 회원가입 시도"),
  EMAIL_OTP_SEND_FAIL(HttpStatus.SERVICE_UNAVAILABLE, "AUTH-006", "인증번호 전송이 실패됐습니다.", "OPT 전송 실패"),
  INCORRECT_EMAIL_OTP(HttpStatus.CONFLICT, "AUTH-007", "잘못된 인증번호입니다.", "OPT 검증 실패"),
  ABNORMAL_ACCESS(HttpStatus.UNAUTHORIZED, "AUTH-008", "로그인이 필요합니다.", "비정상 접근 시도"),
  AUTH_FAIL(HttpStatus.UNAUTHORIZED, "AUTH-009", "로그인이 필요합니다.", "비정상 인증 에러"),
  RE_LOGIN_REQUIRED(
      HttpStatus.UNAUTHORIZED, "AUTH-010", "다시 로그인을 해야합니다.", "리프레시 토큰 유효성 검사가 실패됐습니다."),
  TEMP_USER_DIARY_CREATE_FAIL(
      HttpStatus.FORBIDDEN, "AUTH-011", "캐릭터를 먼저 등록해야 합니다.", "TEMP USER 일기 작성 시도");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
