package com.heartsave.todaktodak_api.common.exception.errorspec.storage;

import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum S3ErrorSpec implements ErrorSpec {
  INVALID_S3_URL(
      HttpStatus.BAD_REQUEST,
      "S3-001",
      "실패했습니다. 계속 실패할 경우, 관리자에 문의 부탁드립니다.",
      "AI 서버가 요청한 URL이 유효하지 않습니다."),
  NON_EXISTED_S3_URL(
      HttpStatus.BAD_REQUEST, "S3-002", "생성된 이미지가 만료됐습니다. 다시 생성 부탁드립니다.", "존재하지 않는 S3 URL입니다.");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
