package com.heartsave.todaktodak_api.domain.ai.webhook.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.config.BaseTestObject;
import com.heartsave.todaktodak_api.common.exception.errorspec.event.EventErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackCharacterRequest;
import com.heartsave.todaktodak_api.domain.ai.callback.service.AiCallbackCharacterService;
import com.heartsave.todaktodak_api.domain.event.constant.EventType;
import com.heartsave.todaktodak_api.domain.event.entity.EventEntity;
import com.heartsave.todaktodak_api.domain.event.exception.EventException;
import com.heartsave.todaktodak_api.domain.event.service.EventService;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.domain.member.repository.CharacterCache;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
final class AiCallbackCharacterServiceTest {
  @Mock private MemberRepository memberRepository;
  @Mock private S3FileStorageManager s3FileStorageManager;
  @Mock private EventService eventService;
  @Mock private CharacterCache characterCache;
  @InjectMocks private AiCallbackCharacterService characterService;

  private MemberEntity member;
  private AiCallbackCharacterRequest request;

  @BeforeEach
  void setup() {
    member = BaseTestObject.createMember();
    request =
        AiCallbackCharacterRequest.builder()
            .characterInfo(member.getCharacterInfo())
            .characterStyle(member.getCharacterStyle())
            .characterProfileImageUrl(member.getCharacterImageUrl())
            .memberId(member.getId())
            .seedNum(member.getCharacterSeed())
            .build();
  }

  @Test
  @DisplayName("캐릭터 웹훅에 대한 알림 성공")
  void cacheTempCharacterAndNotify_success() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    ArgumentCaptor<EventEntity> eventCaptor = ArgumentCaptor.forClass(EventEntity.class);
    when(s3FileStorageManager.parseKeyFrom(anyString())).thenReturn("s3-bucket-key");

    // when
    characterService.saveCharacterAndNotify(request);

    // then
    verify(memberRepository).findById(member.getId());
    verify(eventService).send(eventCaptor.capture());

    // 회원 정보 변경
    assertEquals(request.characterInfo(), member.getCharacterInfo());
    assertEquals(request.characterStyle(), member.getCharacterStyle());
    assertEquals(request.seedNum(), member.getCharacterSeed());

    // 알림 메시지
    EventEntity capturedEvent = eventCaptor.getValue();
    assertAll(
        () -> assertEquals(member, capturedEvent.getMemberEntity()),
        () -> assertEquals(EventType.CHARACTER.getType(), capturedEvent.getEventName()));
  }

  @Test
  @DisplayName("캐릭터 웹훅에 대한 알림 실패 - 이벤트 전송 오류")
  void cacheTempCharacterAndNotify_eventSendFail() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
    doThrow(new EventException(EventErrorSpec.EVENT_CONNECT_FAIL))
        .when(eventService)
        .send(any(EventEntity.class));

    // when
    assertThrows(EventException.class, () -> characterService.saveCharacterAndNotify(request));

    // then
    verify(memberRepository).findById(member.getId());
    verify(eventService).send(any(EventEntity.class));
  }

  @Test
  @DisplayName("캐릭터 웹훅에 대한 알림 실패 - 존재하지 않는 회원")
  void cacheTempCharacterAndNotify_memberNotFound() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());

    // when & then
    assertThrows(
        MemberNotFoundException.class, () -> characterService.saveCharacterAndNotify(request));

    verify(memberRepository).findById(member.getId());
    verify(eventService, never()).send(any(EventEntity.class));
  }
}
