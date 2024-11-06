package com.heartsave.todaktodak_api.common.exception.errorspec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AiErrorSpec implements ErrorSpec {
  IMAGE_PROCESS_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "AI-001", "이미지 생성에 실패했습니다.", "이미지 처리 실패"),
  INVALID_API_KEY(
      HttpStatus.UNAUTHORIZED,
      "AI-002",
      "해당 API KEY는 유효하지 않습니다",
      "AI서버가 유효하지 않은 API KEY로 접근을 시도했습니다.");
  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
