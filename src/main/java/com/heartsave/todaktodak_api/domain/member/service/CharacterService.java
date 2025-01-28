package com.heartsave.todaktodak_api.domain.member.service;

import static com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant.REFRESH_TOKEN_COOKIE_KEY;

import com.heartsave.todaktodak_api.common.exception.errorspec.member.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.security.cookie.CookieUtils;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.jwt.util.JwtUtils;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.domain.ai.client.dto.request.AiClientCharacterRequest;
import com.heartsave.todaktodak_api.domain.ai.client.service.AiClientService;
import com.heartsave.todaktodak_api.domain.member.cache.CharacterCache;
import com.heartsave.todaktodak_api.domain.member.domain.TodakRole;
import com.heartsave.todaktodak_api.domain.member.dto.response.CharacterImageResponse;
import com.heartsave.todaktodak_api.domain.member.dto.response.CharacterRegisterResponse;
import com.heartsave.todaktodak_api.domain.member.entity.CharacterEntity;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.exception.MemberException;
import com.heartsave.todaktodak_api.domain.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterService {
  private static final Logger logger = LoggerFactory.getLogger(CharacterService.class);
  private final AiClientService aiClientService;
  private final S3FileStorageManager s3Manager;
  private final MemberRepository memberRepository;
  private final CharacterCache characterCache;

  // 기존 캐릭터와 생성된 적 있는 캐릭터를 presign하여 전달
  @Transactional(readOnly = true)
  public CharacterImageResponse getCharacterImage(Long memberId) {
    MemberEntity member = findMemberById(memberId);
    return CharacterImageResponse.builder()
        .characterImageUrl(getRegisteredCharacterImageUrl(member))
        .tempCharacterImageUrl(getTempCharacterImageUrl(member))
        .build();
  }

  public void createCharacterImage(MultipartFile file, String characterStyle, Long memberId) {
    MemberEntity member = findMemberById(memberId);
    AiClientCharacterRequest dto =
        AiClientCharacterRequest.builder()
            .characterStyle(characterStyle)
            .memberId(member.getId())
            .build();
    aiClientService.callCharacter(file, dto);
  }

  // 캐싱된 캐릭터 정보를 DB에 반영하고, 임시 캐릭터를 프로필로 지정
  public CharacterRegisterResponse registerCharacterAndChangeRole(
      Long memberId, HttpServletResponse response) {
    MemberEntity member = findMemberById(memberId);

    registerCharacter(member);
    member.updateRole(TodakRole.ROLE_USER.name());
    return reIssueToken(member, response);
  }

  private CharacterRegisterResponse reIssueToken(
      MemberEntity member, HttpServletResponse response) {
    var newUser = createNewTodakUser(member);
    var accessToken = JwtUtils.issueToken(newUser, JwtConstant.ACCESS_TYPE);
    var refreshToken = JwtUtils.issueToken(newUser, JwtConstant.REFRESH_TYPE);

    updateRefreshTokenCookie(response, refreshToken);
    return CharacterRegisterResponse.builder().accessToken(accessToken).build();
  }

  private void registerCharacter(MemberEntity member) {
    CharacterEntity cache =
        characterCache
            .findById(member.getId())
            .orElseThrow(
                () -> new MemberException(MemberErrorSpec.TEMP_CHARACTER_EXPIRED, member.getId()));
    member.updateCharacterInfo(cache);
    s3Manager.replaceCharacterImageUrl(cache.characterImageUrl());
    characterCache.delete(cache);
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
    if (member.getCharacterImageUrl() == null) {
      logger.warn("{}의 캐릭터는 등록된 적이 없습니다.", member.getId());
      return null;
    }
    return getPreSignedCharacterImageUrl(member.getCharacterImageUrl());
  }

  @Nullable
  private String getTempCharacterImageUrl(MemberEntity member) {
    CharacterEntity cache = characterCache.findById(member.getId()).orElse(null);
    if (cache == null) {
      logger.warn("최근에 {}의 캐릭터가 생성된 적이 없습니다.", member.getId());
      return null;
    }
    String tempCharacterImageUrl = cache.characterImageUrl();
    return getPreSignedCharacterImageUrl(tempCharacterImageUrl);
  }

  private String getPreSignedCharacterImageUrl(String url) {
    return s3Manager.preSignedCharacterImageUrlFrom(url);
  }
}
