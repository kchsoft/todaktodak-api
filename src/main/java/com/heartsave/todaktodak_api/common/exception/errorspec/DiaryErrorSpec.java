package com.heartsave.todaktodak_api.common.exception.errorspec;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum DiaryErrorSpec implements ErrorSpec {
  DAILY_WRITING_LIMIT_EXCEED(
      HttpStatus.BAD_REQUEST,
      "DIARY-001",
      "하루 일기 작성 횟수를 초과했습니다.",
      "사용자의 하루 일기 작성 횟수를 초과하여 일기 작성에 실패했습니다."),
  DELETE_NOT_FOUND(
      HttpStatus.BAD_REQUEST,
      "DIARY-002",
      "일기 삭제에 실패했습니다.",
      "사용자가 삭제하려는 일기의 작성자가 아니거나, 삭제하려는 일기가 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
