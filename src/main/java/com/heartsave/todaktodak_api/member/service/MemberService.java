package com.heartsave.todaktodak_api.member.service;

import static com.heartsave.todaktodak_api.common.security.constant.JwtConstant.REFRESH_TOKEN_COOKIE_KEY;
import static com.heartsave.todaktodak_api.common.security.util.CookieUtils.*;

import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.member.dto.request.NicknameUpdateRequest;
import com.heartsave.todaktodak_api.member.dto.response.MemberProfileResponse;
import com.heartsave.todaktodak_api.member.dto.response.NicknameUpdateResponse;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.entity.projection.MemberProfileProjection;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
  private final MemberRepository memberRepository;
  private final S3FileStorageManager s3Manager;

  public NicknameUpdateResponse updateNickname(Long memberId, NicknameUpdateRequest dto) {
    MemberEntity retrievedMember = findMemberById(memberId);
    retrievedMember.updateNickname(dto.nickname());
    return NicknameUpdateResponse.builder().nickname(retrievedMember.getNickname()).build();
  }

  @Transactional(readOnly = true)
  public MemberProfileResponse getMemberProfile(Long memberId) {
    MemberProfileProjection memberProfile = getMemberProfileById(memberId);

    String characterPreSignedUrl =
        s3Manager.preSignedCharacterImageUrlFrom(memberProfile.getCharacterImageUrl());

    return MemberProfileResponse.builder()
        .nickname(memberProfile.getNickname())
        .email(memberProfile.getEmail())
        .characterImageUrl(characterPreSignedUrl)
        .build();
  }

  public void deactivate(HttpServletResponse response, Long memberId) {
    MemberEntity retrievedMember = findMemberById(memberId);
    memberRepository.delete(retrievedMember);
    updateCookie(response, createExpiredCookie(REFRESH_TOKEN_COOKIE_KEY));
  }

  private MemberEntity findMemberById(Long id) {
    return memberRepository
        .findById(id)
        .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, id));
  }

  private MemberProfileProjection getMemberProfileById(Long id) {
    return memberRepository
        .findProjectedById(id)
        .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, id));
  }
}
