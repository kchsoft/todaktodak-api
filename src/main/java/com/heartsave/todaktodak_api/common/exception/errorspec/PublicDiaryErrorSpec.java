package com.heartsave.todaktodak_api.common.exception.errorspec;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum PublicDiaryErrorSpec implements ErrorSpec {
  PUBLIC_DIARY_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      "PUBLIC_DIARY-001",
      "공개 일기를 찾을 수 없습니다.",
      "다음의 공개 일기 목록을 DB에서 조회할 수 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
