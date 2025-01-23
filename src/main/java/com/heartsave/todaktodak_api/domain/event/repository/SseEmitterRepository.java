package com.heartsave.todaktodak_api.domain.event.repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRepository {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  // key: 회원 ID
  private final ConcurrentHashMap<Long, SseEmitter> emitterRepository = new ConcurrentHashMap<>();

  public synchronized SseEmitter put(Long memberId, SseEmitter emitter) {
    SseEmitter oldEmitter = emitterRepository.get(memberId);
    if (oldEmitter != null) {
      logger.info("재연결을 위해 {}의 에미터 연결을 종료합니다.", memberId);
      oldEmitter.complete();
      delete(memberId);
    }
    emitterRepository.put(memberId, emitter);
    logger.info("회원 {}의 Emitter 추가", memberId);
    return emitter;
  }

  public void delete(Long memberId) {
    emitterRepository.remove(memberId);
    logger.info("회원 {}의 Emitter 삭제", memberId);
  }

  public Optional<SseEmitter> get(Long memberId) {
    return Optional.ofNullable(emitterRepository.get(memberId));
  }

  public int getCount() {
    return emitterRepository.size();
  }
}
