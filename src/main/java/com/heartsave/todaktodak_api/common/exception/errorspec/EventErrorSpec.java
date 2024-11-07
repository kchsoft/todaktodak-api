package com.heartsave.todaktodak_api.common.exception.errorspec;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum EventErrorSpec implements ErrorSpec {
  EVENT_CONNECT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "EVENT-001", "알림 연결이 실패됐습니다.", "SSE 연결 실패"),
  EVENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "EVENT-002", "존재하지 않는 알림입니다.", "비정상 이벤트 조회");
  private final HttpStatus status;
  private final String code;
  private final String clientMessage;
  private final String debugMessage;
}
