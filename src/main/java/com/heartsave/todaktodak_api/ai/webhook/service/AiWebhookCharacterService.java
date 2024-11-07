package com.heartsave.todaktodak_api.ai.webhook.service;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiWebhookCharacterRequest;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
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

  public void saveCharacterAndNotify(AiWebhookCharacterRequest dto) {
    saveCharacter(dto);
    // TODO: SSE 알림 구현
  }

  private void saveCharacter(AiWebhookCharacterRequest dto) {
    Long memberId = dto.memberId();
    MemberEntity retrievedMember =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, memberId));
    retrievedMember.updateCharacterInfo(dto.characterInfo(), dto.characterStyle(), dto.seedNum());
  }
}
