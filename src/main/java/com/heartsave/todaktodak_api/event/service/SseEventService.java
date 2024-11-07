package com.heartsave.todaktodak_api.event.service;

import com.heartsave.todaktodak_api.event.entity.EventEntity;
import com.heartsave.todaktodak_api.event.repository.EventRepository;
import com.heartsave.todaktodak_api.event.repository.SseEmitterRepository;
import java.io.IOException;
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

  @Override
  public void send(EventEntity event) {
    emitterRepository
        .get(event.getMemberEntity().getId())
        .ifPresentOrElse(
            emitter -> sendEvent(emitter, event),
            () -> logEmitterNotFound(event.getMemberEntity().getId()));
  }

  private void sendEvent(SseEmitter emitter, EventEntity event) {
    try {
      emitter.send(createEvent(event));
      logEventSuccess(event);
    } catch (IOException e) {
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
    disconnectExistingEmitter(memberId);
    SseEmitter emitter = createEmitter(timeout);
    setEmitterCallbacks(emitter, memberId);

    if (!sendInitialEvent(emitter)) {
      return null;
    }

    return emitterRepository.save(memberId, emitter);
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

  private boolean sendInitialEvent(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event().name(CONNECT_EVENT_NAME).data(CONNECT_MESSAGE));
      return true;
    } catch (IOException e) {
      handleEmitterError(null, e);
      return false;
    }
  }

  private void handleEmitterComplete(SseEmitter emitter, Long memberId, String reason) {
    emitter.complete();
    emitterRepository.delete(memberId);
    logger.info("{}로 인한 SSE 연결. memberId = {}", reason, memberId);
  }

  private void handleEmitterError(Long memberId, Exception e) {
    if (memberId != null) {
      emitterRepository.delete(memberId);
    }
    logger.error("SSE 에러 발생. memberId = {}, error = {}", memberId, e.getMessage());
  }

  private void logEventSuccess(EventEntity event) {
    logger.info(
        "이벤트 전송 완료. memberId = {}, eventName = {}",
        event.getMemberEntity().getId(),
        event.getEventName());
  }

  private void logEmitterNotFound(Long memberId) {
    logger.warn("연결된 Emitter 없음. memberId = {}", memberId);
  }
}
