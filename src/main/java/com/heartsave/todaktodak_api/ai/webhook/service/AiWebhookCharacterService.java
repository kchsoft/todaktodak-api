package com.heartsave.todaktodak_api.ai.webhook.service;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookCharacterCompletionRequest;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.event.constant.EventType;
import com.heartsave.todaktodak_api.event.entity.EventEntity;
import com.heartsave.todaktodak_api.event.service.EventService;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiWebhookCharacterService {
  private final MemberRepository memberRepository;
  private final EventService eventService;

  public void saveCharacterAndNotify(WebhookCharacterCompletionRequest dto) {
    MemberEntity member = saveCharacter(dto);

    eventService.saveAndSend(
        EventEntity.builder()
            .memberEntity(member)
            .eventName(EventType.CHARACTER.getType())
            .eventData("캐릭터가 생성됐습니다.")
            .build());
  }

  private MemberEntity saveCharacter(WebhookCharacterCompletionRequest dto) {
    Long memberId = dto.memberId();
    MemberEntity retrievedMember =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, memberId));
    retrievedMember.updateCharacterInfo(dto.characterInfo(), dto.characterStyle(), dto.seedNum());
    return retrievedMember;
  }
}
