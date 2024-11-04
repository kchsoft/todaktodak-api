package com.heartsave.todaktodak_api.member.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.storage.S3FileStorageService;
import com.heartsave.todaktodak_api.member.dto.response.CharacterTemporaryImageResponse;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterService {
  private final MemberRepository memberRepository;
  private final S3FileStorageService s3Service;

  @Transactional(readOnly = true)
  public CharacterTemporaryImageResponse getPastCharacterImage(TodakUser principal) {
    Long id = principal.getId();
    MemberEntity retrievedMember =
        memberRepository
            .findById(id)
            .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, id));

    return CharacterTemporaryImageResponse.builder()
        .characterImageUrl(
            s3Service.preSignedCharacterImageUrlFrom(retrievedMember.getCharacterImageUrl()))
        .build();
  }
}
