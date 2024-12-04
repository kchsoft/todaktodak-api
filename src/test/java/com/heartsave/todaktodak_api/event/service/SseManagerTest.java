package com.heartsave.todaktodak_api.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.event.repository.SseEmitterRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseManagerTest {
  @Mock private SseEmitterRepository emitterRepository;
  @InjectMocks private SseManager sseManager;

  @Mock private SseEmitter emitter;
  private MemberEntity member;

  private final long TEST_TIMEOUT = 60000L;

  @BeforeEach
  void setup() {
    member = BaseTestObject.createMember();
  }

  @Test
  @DisplayName("SSE 연결")
  void connectTest() {
    // given
    when(emitterRepository.put(eq(member.getId()), any(SseEmitter.class))).thenReturn(emitter);

    // when
    SseEmitter result = sseManager.connect(member.getId(), TEST_TIMEOUT);

    // then
    assertNotNull(result);
    verify(emitterRepository, times(1)).put(eq(member.getId()), any(SseEmitter.class));
  }
}
