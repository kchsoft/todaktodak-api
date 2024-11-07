package com.heartsave.todaktodak_api.common.exception.errorspec;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum S3ErrorSpec implements ErrorSpec {
  INVALID_S3_URL(
      HttpStatus.BAD_REQUEST, "S3-001", "유효하지 않은 S3 URL 입니다.", "AI 서버가 요청한 URL이 유효하지 않습니다.");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
