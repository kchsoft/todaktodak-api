package com.heartsave.todaktodak_api.domain.event.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.event.EventErrorSpec;
import com.heartsave.todaktodak_api.domain.event.exception.EventException;
import com.heartsave.todaktodak_api.domain.event.repository.SseEmitterRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SseManager {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final SseEmitterRepository emitterRepository;
  private static final String CONNECT_EVENT_NAME = "connect";
  private static final String CONNECT_MESSAGE = "이벤트 수신 연결 성공";
  private static final String TIMEOUT_MESSAGE = "시간 초과";
  private static final String COMPLETION_MESSAGE = "전송 완료 또는 재접속";

  public SseEmitter connect(Long memberId, Long timeout) {
    // 에미터 생성 및 콜백 활성화
    SseEmitter emitter = createEmitter(timeout);
    setEmitterCallbacks(emitter, memberId);
    emitterRepository.put(memberId, emitter);
    logger.info("현재 에미터 개수={}", emitterRepository.getCount());

    // 연결 성공 이벤트 전송
    sendConnectEvent(emitter, memberId);
    logger.info("SSE를 위해 회원 {}와 성공적으로 연결됐습니다.", memberId);

    return emitter;
  }

  private SseEmitter createEmitter(Long timeout) {
    SseEmitter sseEmitter = new SseEmitter(timeout);
    logger.info("에미터가 생성됐습니다.");
    return sseEmitter;
  }

  private void setEmitterCallbacks(SseEmitter emitter, Long memberId) {
    emitter.onTimeout(() -> handleEmitterComplete(emitter, memberId, TIMEOUT_MESSAGE));
    emitter.onCompletion(() -> handleEmitterComplete(emitter, memberId, COMPLETION_MESSAGE));
    emitter.onError(
        e -> {
          logger.error("에미터의 에러가 발생했습니다. {}", e.getMessage());
          handleEmitterError(memberId, (Exception) e);
        });
    logger.info("{}의 에미터 콜백 설정 완료.", memberId);
  }

  private void sendConnectEvent(SseEmitter emitter, Long memberId) {
    try {
      emitter.send(SseEmitter.event().name(CONNECT_EVENT_NAME).data(CONNECT_MESSAGE));
    } catch (IOException e) {
      debugLog(e);
      throw new EventException(EventErrorSpec.EVENT_CONNECT_FAIL, memberId);
    } catch (Exception e) {
      debugLog(e);
    }
  }

  private void debugLog(Exception e) {
    logger.error("뭔가 잘못됐음. 예외 타입={}, 예외 메시지={}", e.getCause(), e.getMessage());
  }

  private void handleEmitterComplete(SseEmitter emitter, Long memberId, String reason) {
    try {
      emitter.complete();
      emitterRepository.delete(memberId);
      logger.info("SSE 연결 종료. memberId={} reason={}", memberId, reason);

    } catch (Exception e) {
      logger.warn("{}의 에미터는 이미 지워졌습니다.", memberId);
    }

    //    if (TIMEOUT_MESSAGE.equals(reason))
    //      throw new EventException(EventErrorSpec.CONNECTION_TIMEOUT, memberId);
  }

  private void handleEmitterError(Long memberId, Exception e) {
    if (memberId != null) {
      emitterRepository.delete(memberId);
    }
    logger.error("SSE 에러 발생. memberId={}, error={}", memberId, e.getMessage());
  }
}
