package com.heartsave.todaktodak_api.member.service;

import static com.heartsave.todaktodak_api.common.security.constant.JwtConstant.REFRESH_TOKEN_COOKIE_KEY;

import com.heartsave.todaktodak_api.ai.dto.request.AiCharacterRequest;
import com.heartsave.todaktodak_api.ai.service.AiService;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.security.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.util.CookieUtils;
import com.heartsave.todaktodak_api.common.security.util.JwtUtils;
import com.heartsave.todaktodak_api.common.storage.S3FileStorageService;
import com.heartsave.todaktodak_api.member.domain.TodakRole;
import com.heartsave.todaktodak_api.member.dto.response.CharacterRegisterResponse;
import com.heartsave.todaktodak_api.member.dto.response.CharacterTemporaryImageResponse;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterService {
  private final MemberRepository memberRepository;
  private final AiService aiService;
  private final S3FileStorageService s3Service;

  @Transactional(readOnly = true)
  public CharacterTemporaryImageResponse getPastCharacterImage(TodakUser principal) {
    MemberEntity member = findMemberByPrincipal(principal);
    return CharacterTemporaryImageResponse.builder()
        .characterImageUrl(s3Service.preSignedCharacterImageUrlFrom(member.getCharacterImageUrl()))
        .build();
  }

  public void createCharacterImage(
      TodakUser principal, MultipartFile file, AiCharacterRequest dto) {
    findMemberByPrincipal(principal);
    aiService.callCharacter(file, dto);
  }

  public CharacterRegisterResponse changeRoleAndReissueToken(
      TodakUser principal, HttpServletResponse response) {
    MemberEntity member = findMemberByPrincipal(principal);
    member.updateRole(TodakRole.ROLE_USER.name());

    var newUser = createNewTodakUser(member);
    var accessToken = JwtUtils.issueToken(newUser, JwtConstant.ACCESS_TYPE);
    var refreshToken = JwtUtils.issueToken(newUser, JwtConstant.REFRESH_TYPE);

    updateRefreshTokenCookie(response, refreshToken);
    return CharacterRegisterResponse.builder().accessToken(accessToken).build();
  }

  private MemberEntity findMemberByPrincipal(TodakUser principal) {
    Long id = principal.getId();
    return memberRepository
        .findById(id)
        .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, id));
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
}
