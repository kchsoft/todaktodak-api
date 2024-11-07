package com.heartsave.todaktodak_api.ai.webhook.service;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookCharacterCompletionRequest;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageService;
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
  private final S3FileStorageService s3Service;

  public void saveCharacterAndNotify(WebhookCharacterCompletionRequest dto) {
    saveCharacter(dto);
    // TODO: SSE 알림 구현
  }

  private void saveCharacter(WebhookCharacterCompletionRequest dto) {
    Long memberId = dto.memberId();
    MemberEntity retrievedMember =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, memberId));
    String url = s3Service.parseKeyFrom(dto.characterUrl());
    retrievedMember.updateCharacterInfo(
        dto.characterInfo(), dto.characterStyle(), dto.seedNum(), url);
  }
}
