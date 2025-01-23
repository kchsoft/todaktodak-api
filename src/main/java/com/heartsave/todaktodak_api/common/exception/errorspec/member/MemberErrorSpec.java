package com.heartsave.todaktodak_api.common.exception.errorspec.member;

import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum MemberErrorSpec implements ErrorSpec {
  NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-001", "회원님의 정보를 찾을 수 없습니다.", "서비스에 등록되어 있지 않은 사용자 입니다."),
  TEMP_CHARACTER_EXPIRED(
      HttpStatus.CONFLICT,
      "MEMBER-002",
      "임시 캐릭터가 만료됐습니다. 캐릭터를 다시 생성해주세요.",
      "캐릭터 등록 과정에서 캐시 데이터가 만료됐습니다.");

  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
