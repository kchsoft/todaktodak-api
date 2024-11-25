package com.heartsave.todaktodak_api.member.service;

import static com.heartsave.todaktodak_api.common.constant.CoreConstant.URL.TEMP_CHARACTER_IMAGE_URL_PREFIX;
import static com.heartsave.todaktodak_api.common.security.constant.JwtConstant.REFRESH_TOKEN_COOKIE_KEY;

import com.heartsave.todaktodak_api.ai.client.dto.request.ClientCharacterRequest;
import com.heartsave.todaktodak_api.ai.client.service.AiClientService;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.security.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.util.CookieUtils;
import com.heartsave.todaktodak_api.common.security.util.JwtUtils;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.member.domain.TodakRole;
import com.heartsave.todaktodak_api.member.dto.response.CharacterImageResponse;
import com.heartsave.todaktodak_api.member.dto.response.CharacterRegisterResponse;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.CharacterCacheRepository;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterService {
  private final AiClientService aiClientService;
  private final S3FileStorageManager s3Manager;
  private final MemberRepository memberRepository;
  private final CharacterCacheRepository characterCacheRepository;

  // 기존 캐릭터와 생성된 적 있는 캐릭터를 presign하여 전달
  @Transactional(readOnly = true)
  public CharacterImageResponse getCharacterImage(Long memberId) {
    MemberEntity member = findMemberById(memberId);
    return CharacterImageResponse.builder()
        .characterImageUrl(getRegisteredCharacterImageUrl(member))
        .tempCharacterImageUrl(getTempCharacterImageUrl(member))
        .build();
  }

  public void createCharacterImage(MultipartFile file, Long memberId) {
    MemberEntity member = findMemberById(memberId);
    ClientCharacterRequest dto =
        ClientCharacterRequest.builder().characterStyle("romance").memberId(member.getId()).build();
    aiClientService.callCharacter(file, dto);
  }

  public CharacterRegisterResponse changeRoleAndReissueToken(
      Long memberId, HttpServletResponse response) {
    MemberEntity member = findMemberById(memberId);
    member.updateRole(TodakRole.ROLE_USER.name());

    var newUser = createNewTodakUser(member);
    var accessToken = JwtUtils.issueToken(newUser, JwtConstant.ACCESS_TYPE);
    var refreshToken = JwtUtils.issueToken(newUser, JwtConstant.REFRESH_TYPE);

    updateRefreshTokenCookie(response, refreshToken);
    return CharacterRegisterResponse.builder().accessToken(accessToken).build();
  }

  private MemberEntity findMemberById(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, memberId));
  }

  private TodakUser createNewTodakUser(MemberEntity member) {
    return TodakUser.builder()
        .id(member.getId())
        .username(member.getLoginId())
        .role(member.getRole().name())
        .build();
  }

  private void updateRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    var refreshCookie = CookieUtils.createValidCookie(REFRESH_TOKEN_COOKIE_KEY, refreshToken);
    CookieUtils.updateCookie(response, refreshCookie);
  }

  private String getRegisteredCharacterImageUrl(MemberEntity member) {
    if (member.getCharacterImageUrl() == null) return null;
    return getPreSignedCharacterImageUrl(member.getCharacterImageUrl());
  }

  @Nullable
  private String getTempCharacterImageUrl(MemberEntity member) {
    if (!characterCacheRepository.existsById(member.getId())) return null;
    String tempCharacterImageUrl = TEMP_CHARACTER_IMAGE_URL_PREFIX + member.getCharacterImageUrl();
    return getPreSignedCharacterImageUrl(tempCharacterImageUrl);
  }

  private String getPreSignedCharacterImageUrl(String url) {
    return s3Manager.preSignedCharacterImageUrlFrom(url);
  }
}
