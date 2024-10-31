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
    // 토큰 유효성 검사
    var refreshToken = extractRefreshToken(request);
    validateToken(refreshToken);

    // 인증 정보 생성
    Long id = JwtUtils.extractSubject(refreshToken);
    TodakUser user = createAuthenticationFromMember(id);

    // 토큰 재발급 및 쿠키 갱신
    var accessToken = JwtUtils.issueToken(user, ACCESS_TYPE);
    var newRefreshToken = JwtUtils.issueToken(user, REFRESH_TYPE);
    updateRefreshTokenCookie(response, newRefreshToken);

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

  private String extractRefreshToken(HttpServletRequest request) {
    var refreshTokenCookie =
        CookieUtils.extractCookie(request, JwtConstant.REFRESH_TOKEN_COOKIE_KEY);
    if (refreshTokenCookie == null) {
      throw new AuthException(AuthErrorSpec.ABNORMAL_ACCESS);
    }
    String token = refreshTokenCookie.getValue();
    if (token == null) {
      throw new AuthException(AuthErrorSpec.ABNORMAL_ACCESS);
    }
    return token;
  }

  private void validateToken(String token) {
    try {
      JwtUtils.extractAllClaims(token);
      if (!isRefreshTokenType(token)) {
        throw new AuthException(AuthErrorSpec.ABNORMAL_ACCESS);
      }
    } catch (ExpiredJwtException e) {
      logger.error("토큰이 만료됐습니다. {}", token);
      throw new AuthException(AuthErrorSpec.RE_LOGIN_REQUIRED);
    } catch (Exception e) {
      logger.error("유효하지 않은 토큰입니다. {}", token);
      throw new AuthException(AuthErrorSpec.ABNORMAL_ACCESS);
    }
  }

  private TodakUser createAuthenticationFromMember(Long id) {
    MemberEntity member =
        memberRepository.findById(id).orElseThrow(() -> new AuthException(AuthErrorSpec.AUTH_FAIL));
    return TodakUser.builder()
        .id(member.getId())
        .username(member.getLoginId())
        .role(member.getRole().name())
        .build();
  }

  private void updateRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
    var refreshCookie = CookieUtils.createValidCookie(REFRESH_TOKEN_COOKIE_KEY, refreshToken);
    response.setContentType(APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(UTF_8.name());
    response.addCookie(refreshCookie);
    response.setStatus(SC_OK);
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
