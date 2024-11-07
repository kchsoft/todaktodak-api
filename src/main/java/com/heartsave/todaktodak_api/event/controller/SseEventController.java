package com.heartsave.todaktodak_api.event.controller;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.event.service.SseEventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "알림", description = "클라이언트에게 알림 이벤트를 전송합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/event")
public class SseEventController {
  private final SseEventService eventService;
  private final Long SSE_TIMEOUT_MILLI_SECOND = 120 * 1000L;

  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@AuthenticationPrincipal TodakUser principal) {
    return eventService.connect(principal.getId(), SSE_TIMEOUT_MILLI_SECOND);
  }
}
