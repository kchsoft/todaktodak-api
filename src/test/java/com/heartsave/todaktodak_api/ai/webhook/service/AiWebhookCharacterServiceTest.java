package com.heartsave.todaktodak_api.ai.webhook.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookCharacterCompletionRequest;
import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.event.service.EventService;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
final class AiWebhookCharacterServiceTest {
  @Mock private MemberRepository memberRepository;
  @Mock private EventService eventService;
  @InjectMocks private AiWebhookCharacterService characterService;

  private MemberEntity member;
  private WebhookCharacterCompletionRequest request;

  @BeforeEach
  void setup() {
    member = BaseTestObject.createMember();
    request =
        WebhookCharacterCompletionRequest.builder()
            .characterInfo(member.getCharacterInfo())
            .characterStyle(member.getCharacterStyle())
            .characterUrl(member.getCharacterImageUrl())
            .memberId(member.getId())
            .seedNum(member.getCharacterSeed())
            .build();
  }

  @Test
  void saveCharacterAndNotify() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

    // when
    characterService.saveCharacterAndNotify(request);

    // then
    verify(memberRepository).findById(member.getId());
    assertEquals(request.characterInfo(), member.getCharacterInfo());
    assertEquals(request.characterStyle(), member.getCharacterStyle());
    assertEquals(request.seedNum(), member.getCharacterSeed());
  }

  @Test
  void saveCharacterAndNotify_memberNotFound() {
    // given
    when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());

    // when & then
    assertThrows(
        MemberNotFoundException.class, () -> characterService.saveCharacterAndNotify(request));

    verify(memberRepository).findById(member.getId());
  }
}
