package com.heartsave.todaktodak_api.auth.service;

import static com.heartsave.todaktodak_api.common.security.constant.JwtConstant.*;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.heartsave.todaktodak_api.auth.dto.request.SignUpRequest;
import com.heartsave.todaktodak_api.auth.dto.response.TokenReissueResponse;
import com.heartsave.todaktodak_api.auth.exception.AuthException;
import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
import com.heartsave.todaktodak_api.common.security.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.domain.AuthType;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.util.CookieUtils;
import com.heartsave.todaktodak_api.common.security.util.JwtUtils;
import com.heartsave.todaktodak_api.member.domain.TodakRole;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public TokenReissueResponse reissueToken(
      HttpServletRequest request, HttpServletResponse response) {
    var retrievedRefreshTokenCookie =
        CookieUtils.extractCookie(request, JwtConstant.REFRESH_TOKEN_COOKIE_KEY);
    if (retrievedRefreshTokenCookie == null) throw new AuthException(AuthErrorSpec.ABNORMAL_ACCESS);

    var token = retrievedRefreshTokenCookie.getValue();
    logger.info("리프레시 토큰: {}", token);
    if (token == null) throw new AuthException(AuthErrorSpec.RE_LOGIN_REQUIRED);
    try {
      JwtUtils.extractAllClaims(token);
      if (!isRefreshTokenType(token)) throw new AuthException(AuthErrorSpec.RE_LOGIN_REQUIRED);
    } catch (ExpiredJwtException e) {
      logger.error("토큰이 만료됐습니다. {}", token);
      throw new AuthException(AuthErrorSpec.RE_LOGIN_REQUIRED);
    } catch (Exception e) {
      logger.error("유효하지 않은 토큰입니다. {}", token);
      throw new AuthException(AuthErrorSpec.RE_LOGIN_REQUIRED);
    }

    Long id = JwtUtils.extractSubject(token);
    MemberEntity member =
        memberRepository.findById(id).orElseThrow(() -> new AuthException(AuthErrorSpec.AUTH_FAIL));
    var user =
        TodakUser.builder()
            .id(member.getId())
            .username(member.getLoginId())
            .role(member.getRole().name())
            .build();
    var accessToken = JwtUtils.issueToken(user, ACCESS_TYPE);
    var refreshToken = JwtUtils.issueToken(user, REFRESH_TYPE);
    var refreshCookie = CookieUtils.createValidCookie(REFRESH_TOKEN_COOKIE_KEY, refreshToken);
    response.setContentType(APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(UTF_8.name());
    response.addCookie(refreshCookie);
    response.setStatus(SC_OK);

    return TokenReissueResponse.builder().accessToken(accessToken).build();
  }

  public void signUp(SignUpRequest dto) {
    if (isDuplicated(dto)) throw new AuthException(AuthErrorSpec.DUPLICATED_INFORMATION, dto);

    var newMember =
        MemberEntity.builder()
            .authType(AuthType.BASE)
            .email(dto.email())
            .nickname(dto.nickname())
            .loginId(dto.loginId())
            .password(passwordEncoder.encode(dto.password()))
            .role(TodakRole.ROLE_USER)
            .build();

    memberRepository.save(newMember);
  }

  private boolean isRefreshTokenType(String token) {
    return JwtUtils.extractType(token).equals(JwtConstant.REFRESH_TYPE);
  }

  private boolean isDuplicated(SignUpRequest dto) {
    return isDuplicatedEmail(dto.email())
        || isDuplicatedLoginId(dto.loginId())
        || isDuplicatedNickname(dto.nickname());
  }

  public boolean isDuplicatedLoginId(String loginId) {
    return memberRepository.findMemberEntityByLoginId(loginId).isPresent();
  }

  public boolean isDuplicatedNickname(String nickname) {
    return memberRepository.findMemberEntityByNickname(nickname).isPresent();
  }

  public boolean isDuplicatedEmail(String email) {
    return memberRepository.findMemberEntityByEmail(email).isPresent();
  }
}
