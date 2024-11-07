package com.heartsave.todaktodak_api.event.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.EventErrorSpec;
import com.heartsave.todaktodak_api.event.entity.EventEntity;
import com.heartsave.todaktodak_api.event.exception.EventException;
import com.heartsave.todaktodak_api.event.repository.EventRepository;
import com.heartsave.todaktodak_api.event.repository.SseEmitterRepository;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional
public class SseEventService implements EventService {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final EventRepository eventRepository;
  private final SseEmitterRepository emitterRepository;
  private static final String CONNECT_EVENT_NAME = "connect";
  private static final String CONNECT_MESSAGE = "이벤트 수신 연결 성공";
  private static final String TIMEOUT_MESSAGE = "시간 초과";
  private static final String COMPLETION_MESSAGE = "전송 완료 또는 재접속";

  @Override
  public void save(EventEntity event) {
    eventRepository.save(event);
  }

  // 연결되어 있으면 이벤트를 전송하고,
  // 그렇지 않으면 이벤트를 저장한다.
  @Override
  public void send(EventEntity event) {
    emitterRepository
        .get(event.getMemberEntity().getId())
        .ifPresentOrElse(
            emitter -> sendEvent(emitter, event),
            () -> {
              save(event);
              logEmitterNotFound(event.getMemberEntity().getId());
            });
  }

  private void sendEvent(SseEmitter emitter, EventEntity event) {
    try {
      emitter.send(createEvent(event));
      logEventSuccess(event);
    } catch (IOException e) {
      save(event);
      handleEmitterError(event.getMemberEntity().getId(), e);
    }
  }

  private SseEmitter.SseEventBuilder createEvent(EventEntity event) {
    return SseEmitter.event()
        .id(event.getId())
        .name(event.getEventName())
        .data(event.getEventData());
  }

  public SseEmitter connect(Long memberId, Long timeout) {
    // 기존 연결 존재시 제거
    disconnectExistingEmitter(memberId);

    // 연결 관리 및 콜백 활성화
    SseEmitter emitter = createEmitter(timeout);
    setEmitterCallbacks(emitter, memberId);
    emitterRepository.save(emitter, memberId);

    // 연결 성공 이벤트 전송
    sendConnectEvent(emitter, memberId);

    // 미수신 이벤트 전송
    sendPastEvent(emitter, memberId);

    return emitter;
  }

  private void disconnectExistingEmitter(Long memberId) {
    emitterRepository
        .get(memberId)
        .ifPresent(
            emitter -> {
              emitter.complete();
              emitterRepository.delete(memberId);
            });
  }

  private SseEmitter createEmitter(Long timeout) {
    return new SseEmitter(timeout);
  }

  private void setEmitterCallbacks(SseEmitter emitter, Long memberId) {
    emitter.onTimeout(() -> handleEmitterComplete(emitter, memberId, TIMEOUT_MESSAGE));
    emitter.onCompletion(() -> handleEmitterComplete(emitter, memberId, COMPLETION_MESSAGE));
    emitter.onError(e -> handleEmitterError(memberId, (Exception) e));
  }

  private void sendConnectEvent(SseEmitter emitter, Long memberId) {
    try {
      emitter.send(SseEmitter.event().name(CONNECT_EVENT_NAME).data(CONNECT_MESSAGE));
    } catch (IOException e) {
      throw new EventException(EventErrorSpec.EVENT_CONNECT_FAIL, memberId);
    }
  }

  private void sendPastEvent(SseEmitter emitter, Long memberId) {
    // 수신 후 이벤트 제거
    List<EventEntity> pastEvents = eventRepository.findAllEventsByMemberId(memberId);
    pastEvents.forEach(
        event -> {
          sendEvent(emitter, event);
          eventRepository.delete(event);
        });
  }

  private void handleEmitterComplete(SseEmitter emitter, Long memberId, String reason) {
    emitter.complete();
    emitterRepository.delete(memberId);
    logger.info("SSE 연결 종료. memberId={} reason={}", memberId, reason);

    if (TIMEOUT_MESSAGE.equals(reason))
      throw new EventException(EventErrorSpec.CONNECTION_TIMEOUT, memberId);
  }

  private void handleEmitterError(Long memberId, Exception e) {
    if (memberId != null) {
      emitterRepository.delete(memberId);
    }
    logger.error("SSE 에러 발생. memberId={}, error={}", memberId, e.getMessage());
  }

  private void logEventSuccess(EventEntity event) {
    logger.info(
        "이벤트 전송 완료. memberId={}, eventName={}",
        event.getMemberEntity().getId(),
        event.getEventName());
  }

  private void logEmitterNotFound(Long memberId) {
    logger.warn("연결된 Emitter 없음. memberId={}", memberId);
  }
}
