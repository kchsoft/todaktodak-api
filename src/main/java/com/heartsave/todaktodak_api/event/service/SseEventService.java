package com.heartsave.todaktodak_api.event.service;

import com.heartsave.todaktodak_api.event.entity.EventEntity;
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
public class SseEventService implements EventService {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final EventRepository eventRepository;
  private final SseEmitterRepository emitterRepository;

  @Override
  public void save(EventEntity event) {
    logger.info("{}에 대한 이벤트를 저장합니다.", event.getMemberEntity().getId());
    eventRepository.save(event);
  }

  // 연결되어 있으면 이벤트를 전송하고,
  // 그렇지 않으면 이벤트를 저장한다.
  @Transactional
  @Override
  public void send(EventEntity event) {
    SseEmitter emitter = emitterRepository.get(event.getMemberEntity().getId()).orElse(null);
    sendOrSave(emitter, event);
  }

  @Transactional
  public void sendPastEvent(SseEmitter emitter, Long memberId) {
    try {
      // 수신 후 이벤트 제거
      List<EventEntity> pastEvents = eventRepository.findAllEventsByMemberId(memberId);
      pastEvents.forEach(
          event -> {
            sendOrSave(emitter, event);
            eventRepository.delete(event);
          });
      logger.info("{}가 수신하지 않은 메시지를 전부 전송했습니다.", memberId);
    } catch (Exception e) {
      debugLog(e);
    }
  }

  private void sendOrSave(SseEmitter emitter, EventEntity event) {
    if (emitter == null) {
      save(event);
      logEmitterNotFound(event.getMemberEntity().getId());
      return;
    }

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

  private void debugLog(Exception e) {
    logger.error("뭔가 잘못됐음. 예외 타입={}, 예외 메시지={}", e.getCause(), e.getMessage());
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
