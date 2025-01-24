package com.heartsave.todaktodak_api.domain.event.repository;

import static org.assertj.core.api.Assertions.*;

import com.heartsave.todaktodak_api.domain.event.repository.SseEmitterRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

final class SseEmitterRepositoryTest {
  private SseEmitterRepository emitterRepository;
  private SseEmitter sseEmitter;
  private static final Long MEMBER_ID = 1L;

  @BeforeEach
  void setUp() {
    emitterRepository = new SseEmitterRepository();
    sseEmitter = new SseEmitter();
  }

  @Test
  @DisplayName("연결 관리를 위한 Emitter 저장 성공")
  void saveTest() {
    // when
    SseEmitter savedEmitter = emitterRepository.put(MEMBER_ID, sseEmitter);
    int emitterCount = emitterRepository.getCount();

    // then
    assertThat(savedEmitter).isNotNull();
    assertThat(savedEmitter).isEqualTo(sseEmitter);
    assertThat(emitterCount).isEqualTo(1);
  }

  @Test
  @DisplayName("연결 종료로 인한 Emitter 삭제 성공")
  void deleteTest() {
    // given
    emitterRepository.put(MEMBER_ID, sseEmitter);

    // when
    emitterRepository.delete(MEMBER_ID);
    Optional<SseEmitter> result = emitterRepository.get(MEMBER_ID);
    int emitterCount = emitterRepository.getCount();

    // then
    assertThat(result).isEmpty();
    assertThat(emitterCount).isEqualTo(0);
  }

  @Test
  @DisplayName("존재하지 않는 회원의 Emitter 조회")
  void get_nonExistedMember() {
    // when
    Optional<SseEmitter> result = emitterRepository.get(MEMBER_ID);

    // then
    assertThat(result).isEmpty();
  }
}
