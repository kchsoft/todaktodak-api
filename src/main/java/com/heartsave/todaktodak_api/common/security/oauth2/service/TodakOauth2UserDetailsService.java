package com.heartsave.todaktodak_api.common.security.oauth2.service;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.oauth2.constant.Oauth2ErrorConstant;
import com.heartsave.todaktodak_api.common.security.oauth2.domain.TodakOauth2Attribute;
import com.heartsave.todaktodak_api.domain.member.domain.TodakRole;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

// 리소스 -> MemberEntity와 TodakUser로 변환
//
@Service
@RequiredArgsConstructor
public class TodakOauth2UserDetailsService extends DefaultOAuth2UserService {
  private final MemberRepository memberRepository;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
    OAuth2User user = super.loadUser(request);
    TodakOauth2Attribute attribute =
        TodakOauth2Attribute.of(
            user.getAttributes(),
            request.getClientRegistration().getRegistrationId().toUpperCase());
    if (attribute == null) return null;
    logger.info("OAUTH2 RESOURCE - {}", attribute);
    // 회원 저장
    MemberEntity member = getOrSave(attribute);
    if (!member.getAuthType().equals(attribute.getAuthType())) {
      throw new OAuth2AuthenticationException(Oauth2ErrorConstant.DUPLICATED_EMAIL_ERROR);
    }
    // 저장 성공시 인증 정보 생성
    return TodakUser.builder()
        .id(member.getId())
        .username(member.getLoginId())
        .role(member.getRole().name())
        .attributes(Map.of())
        .build();
  }

  private MemberEntity getOrSave(TodakOauth2Attribute attribute) {
    MemberEntity retrievedMember =
        memberRepository.findMemberEntityByEmail(attribute.getEmail()).orElse(null);
    if (retrievedMember == null) return memberRepository.save(createMember(attribute));
    return retrievedMember;
  }

  private MemberEntity createMember(TodakOauth2Attribute attribute) {
    return MemberEntity.builder()
        .authType(attribute.getAuthType())
        .email(attribute.getEmail())
        .role(TodakRole.ROLE_TEMP)
        .nickname(createNickname(attribute.getEmail(), attribute.getAuthType().name()))
        .loginId(attribute.getUsername())
        .build();
  }

  private String createNickname(String email, String authType) {
    return email.split("@")[0] + "_" + authType;
  }
}
