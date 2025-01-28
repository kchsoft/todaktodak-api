package com.heartsave.todaktodak_api.domain.event.controller;

import com.heartsave.todaktodak_api.domain.auth.annotation.TodakUserId;
import com.heartsave.todaktodak_api.domain.event.service.SseEventService;
import com.heartsave.todaktodak_api.domain.event.service.SseManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "알림", description = "클라이언트에게 알림 이벤트를 전송합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/event")
public class SseEventController {
  private static final Logger logger = LoggerFactory.getLogger(SseEventController.class);
  private final SseManager sseManager;
  private final SseEventService eventService;
  private final long SSE_TIMEOUT_MILLI_SECOND = 300_000L;

  @Operation(summary = "SSE 이벤트 수신을 위한 연결")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "연결 성공",
            content =
                @Content(
                    examples = {
                      @ExampleObject(
                          name = "연결 성공 이벤트",
                          description = "초기 연결 성공시 전송되는 이벤트",
                          value =
                              """
                                 id: memberId_timestamp
                                 event: connect
                                 data: 이벤트 수신 연결 성공
                                 """)
                    })),
        @ApiResponse(responseCode = "500", description = "연결 실패 또는 시간초과")
      })
  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter connect(@TodakUserId Long memberId) {
    logger.info("{}와의 SSE 연결을 시작합니다.", memberId);
    SseEmitter emitter = sseManager.connect(memberId, SSE_TIMEOUT_MILLI_SECOND);
    eventService.sendPastEvent(emitter, memberId);
    return emitter;
  }
}
