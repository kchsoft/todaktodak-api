package com.heartsave.todaktodak_api.common.exception.errorspec.diary;

import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;
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
      "다음의 공개 일기 목록을 DB에서 조회할 수 없습니다."),
  PUBLIC_DIARY_EXIST(
      HttpStatus.BAD_REQUEST,
      "PUBLIC_DIARY-002",
      "이미 공개된 일기가 있습니다.",
      "하나의 일기에 대해 2번 이상 공개 작성을 하려는 시도입니다."),
  PUBLIC_DIARY_DELETE_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      "PUBLIC_DIARY-003",
      "공개 일기 삭제에 실패했습니다.",
      "사용자가 삭제하려는 공개 일기의 작성자가 아니거나, 삭제하려는 공개 일기가 없습니다.");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
