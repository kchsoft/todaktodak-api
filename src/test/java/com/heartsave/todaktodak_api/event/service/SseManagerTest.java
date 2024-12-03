package com.heartsave.todaktodak_api.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.event.entity.EventEntity;
import com.heartsave.todaktodak_api.event.repository.EventRepository;
import com.heartsave.todaktodak_api.event.repository.SseEmitterRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class SseEventServiceSuccessTest {
  @Mock private EventRepository eventRepository;
  @Mock private SseEmitterRepository emitterRepository;
  @InjectMocks private SseEventService sseEventService;

  @Mock private SseEmitter emitter;
  private MemberEntity member;
  private EventEntity event;

  private final long TEST_TIMEOUT = 60000L;

  @BeforeEach
  void setup() {
    member = BaseTestObject.createMember();
    event = EventEntity.builder().memberEntity(member).build();
  }

  @Test
  @DisplayName("이벤트 저장")
  void saveTest() {
    // given
    when(eventRepository.save(any(EventEntity.class))).thenReturn(event);

    // when
    sseEventService.save(event);

    // then
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  @DisplayName("이벤트 전송 - Emitter가 존재하는 경우")
  void send_existedEmitter() throws Exception {
    // given
    when(emitterRepository.get(anyLong())).thenReturn(Optional.of(emitter));

    // when
    sseEventService.send(event);

    // then
    verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
  }

  @Test
  @DisplayName("이벤트 전송 - Emitter가 존재하지 않으면 이벤트를 저장한다.")
  void send_noEmitter_saveEvent() {
    // given
    when(emitterRepository.get(anyLong())).thenReturn(Optional.empty());

    // when
    sseEventService.send(event);

    // then
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  @DisplayName("SSE 연결")
  void connectTest() {
    // given
    when(emitterRepository.save(any(SseEmitter.class), eq(member.getId()))).thenReturn(emitter);
    when(eventRepository.findAllEventsByMemberId(eq(member.getId())))
        .thenReturn(Collections.emptyList());

    // when
    SseEmitter result = sseEventService.connect(member.getId(), TEST_TIMEOUT);

    // then
    assertNotNull(result);
    verify(emitterRepository, times(1)).save(any(SseEmitter.class), eq(member.getId()));
  }

  @Test
  @DisplayName("기존 연결 해제")
  void disconnect_existedEmitter() {
    // given
    when(emitterRepository.get(eq(member.getId()))).thenReturn(Optional.of(emitter));

    // when
    sseEventService.connect(member.getId(), TEST_TIMEOUT);

    // then
    verify(emitter, times(1)).complete();
    verify(emitterRepository, times(1)).delete(member.getId());
  }

  @Test
  @DisplayName("과거 이벤트 전송")
  void sendPastEventTest() {
    // given
    List<EventEntity> pastEvents = Arrays.asList(event, event); // 2개를 수신하지 못한 상황
    when(eventRepository.findAllEventsByMemberId(anyLong())).thenReturn(pastEvents);
    when(emitterRepository.save(any(SseEmitter.class), anyLong())).thenReturn(emitter);

    // when
    sseEventService.connect(member.getId(), TEST_TIMEOUT);

    // then
    verify(eventRepository, times(1)).findAllEventsByMemberId(member.getId());
    verify(eventRepository, times(pastEvents.size())).delete(event);
  }
}
