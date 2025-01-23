package com.heartsave.todaktodak_api.domain.ai.callback.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.member.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackCharacterRequest;
import com.heartsave.todaktodak_api.domain.event.constant.EventType;
import com.heartsave.todaktodak_api.domain.event.entity.EventEntity;
import com.heartsave.todaktodak_api.domain.event.service.EventService;
import com.heartsave.todaktodak_api.domain.member.entity.CharacterEntity;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.domain.member.repository.CharacterCache;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AiCallbackCharacterService {
  private final EventService eventService;
  private final S3FileStorageManager s3Manager;
  private final MemberRepository memberRepository;
  private final CharacterCache characterCache;

  public void saveCharacterAndNotify(AiCallbackCharacterRequest dto) {
    MemberEntity member = getMember(dto);
    cacheTempCharacter(dto);

    eventService.send(
        EventEntity.builder()
            .memberEntity(member)
            .eventName(EventType.CHARACTER.getType())
            .eventData("캐릭터가 생성됐습니다.")
            .build());
  }

  private MemberEntity getMember(AiCallbackCharacterRequest dto) {
    return memberRepository
        .findById(dto.memberId())
        .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, dto.memberId()));
  }

  private void cacheTempCharacter(AiCallbackCharacterRequest dto) {
    String url = s3Manager.parseKeyFrom(dto.characterProfileImageUrl());
    characterCache.save(
        CharacterEntity.builder()
            .id(dto.memberId())
            .characterInfo(dto.characterInfo())
            .characterStyle(dto.characterStyle())
            .characterSeed(dto.seedNum())
            .characterImageUrl(url)
            .build());
  }
}
