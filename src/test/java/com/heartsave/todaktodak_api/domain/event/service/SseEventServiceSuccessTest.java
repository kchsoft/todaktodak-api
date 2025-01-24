package com.heartsave.todaktodak_api.domain.event.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.heartsave.todaktodak_api.config.BaseTestObject;
import com.heartsave.todaktodak_api.domain.event.entity.EventEntity;
import com.heartsave.todaktodak_api.domain.event.repository.EventRepository;
import com.heartsave.todaktodak_api.domain.event.repository.SseEmitterRepository;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import java.io.IOException;
import java.util.Arrays;
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
final class SseEventServiceSuccessTest {
  @Mock private EventRepository eventRepository;
  @Mock private SseEmitterRepository emitterRepository;
  @InjectMocks private SseEventService eventService;

  private SseEmitter emitter;
  private MemberEntity member;
  private EventEntity event;

  private final long TEST_TIMEOUT = 60000L;

  @BeforeEach
  void setup() {
    emitter = mock(SseEmitter.class);
    member = BaseTestObject.createMember();
    event = EventEntity.builder().memberEntity(member).build();
  }

  @Test
  @DisplayName("이벤트 저장")
  void saveTest() {
    // given
    when(eventRepository.save(any(EventEntity.class))).thenReturn(event);

    // when
    eventService.save(event);

    // then
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  @DisplayName("이벤트 전송 - Emitter가 존재하는 경우")
  void send_existedEmitter() throws Exception {
    // given
    when(emitterRepository.get(anyLong())).thenReturn(Optional.of(emitter));

    // when
    eventService.send(event);

    // then
    verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
  }

  @Test
  @DisplayName("이벤트 전송 - Emitter가 존재하지 않으면 이벤트를 저장한다.")
  void send_noEmitter_saveEvent() {
    // given
    when(emitterRepository.get(anyLong())).thenReturn(Optional.empty());

    // when
    eventService.send(event);

    // then
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  @DisplayName("과거 이벤트 전송")
  void sendPastEventTest() throws IOException {
    // given
    List<EventEntity> pastEvents =
        Arrays.asList(event, EventEntity.builder().memberEntity(member).build()); // 2개를 수신하지 못한 상황
    when(eventRepository.findAllEventsByMemberId(anyLong())).thenReturn(pastEvents);

    // when
    eventService.sendPastEvent(emitter, member.getId());

    // then
    verify(eventRepository, times(1)).findAllEventsByMemberId(member.getId());
    verify(emitter, times(pastEvents.size())).send(any(SseEmitter.SseEventBuilder.class));
    verify(eventRepository, times(pastEvents.size())).delete(any(EventEntity.class));
  }
}
