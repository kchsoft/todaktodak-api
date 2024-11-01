package com.heartsave.todaktodak_api.member.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.member.dto.request.NicknameUpdateRequest;
import com.heartsave.todaktodak_api.member.dto.response.MemberProfileResponse;
import com.heartsave.todaktodak_api.member.dto.response.NicknameUpdateResponse;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.entity.projection.MemberProfileProjection;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
  private final MemberRepository memberRepository;

  @Transactional
  public NicknameUpdateResponse updateNickname(TodakUser principal, NicknameUpdateRequest dto) {
    MemberEntity retrievedMember = findMemberById(principal.getId());
    retrievedMember.updateNickname(dto.nickname());
    return NicknameUpdateResponse.builder().nickname(retrievedMember.getNickname()).build();
  }

  public MemberProfileResponse getMemberProfile(TodakUser principal) {
    MemberProfileProjection memberProfile = getMemberProfile(principal.getId());

    String characterPreSignedUrl =
        createCharacterPreSignedUrl(memberProfile.getCharacterImageUrl());

    return MemberProfileResponse.builder()
        .nickname(memberProfile.getNickname())
        .email(memberProfile.getEmail())
        .characterImageUrl(characterPreSignedUrl)
        .build();
  }

  private MemberEntity findMemberById(Long id) {
    return memberRepository
        .findById(id)
        .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, id));
  }

  private MemberProfileProjection getMemberProfile(Long id) {
    return memberRepository
        .findProjectedById(id)
        .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, id));
  }

  private String createCharacterPreSignedUrl(String originUrl) {
    // TODO: presigned url 생성
    return originUrl == null ? "DEFAULT" : "CHANGED";
  }
}
