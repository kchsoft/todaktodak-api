package com.heartsave.todaktodak_api.common.exception.errorspec;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DiaryReactionErrorSpec implements ErrorSpec {
  DIARY_REACTION_EXIST(
      HttpStatus.BAD_REQUEST,
      "DIARY_REACTION-001",
      "잠시 후 다시 시도해 주세요.",
      "일기 반응 요청이 동시에 요청되어 데이터 무결성 예외가 발생했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
