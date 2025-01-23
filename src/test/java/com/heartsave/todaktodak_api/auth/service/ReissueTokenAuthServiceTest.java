package com.heartsave.todaktodak_api.auth.service;

import static com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant.REFRESH_TOKEN_COOKIE_KEY;
import static com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant.REFRESH_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.common.exception.errorspec.auth.AuthErrorSpec;
import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.security.cookie.CookieUtils;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.jwt.util.JwtUtils;
import com.heartsave.todaktodak_api.common.security.util.UtilConfig;
import com.heartsave.todaktodak_api.domain.auth.cache.RefreshTokenCache;
import com.heartsave.todaktodak_api.domain.auth.dto.response.TokenReissueResponse;
import com.heartsave.todaktodak_api.domain.auth.exception.AuthException;
import com.heartsave.todaktodak_api.domain.auth.service.AuthService;
import com.heartsave.todaktodak_api.domain.member.domain.TodakRole;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ReissueTokenAuthServiceTest {
  @Autowired private AuthService authService;
  @MockBean RefreshTokenCache cacheRepository;
  @MockBean MemberRepository memberRepository;
  private static MemberEntity member;
  MockHttpServletRequest request;
  MockHttpServletResponse response;

  @BeforeEach
  void setup() {
    member = MemberEntity.builder().id(1L).loginId("testUser").role(TodakRole.ROLE_USER).build();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @AfterEach
  void teardown() {
    UtilConfig.utilSetup();
  }

  @Test
  @DisplayName("유효한 리프레시 토큰에 대해 토큰 재발급 및 캐시 토큰 변경")
  @WithMockTodakUser
  void reissueTokenSuccessTest() {
    // given
    TodakUser user =
        (TodakUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var oldRefreshToken = JwtUtils.issueToken(user, REFRESH_TYPE);
    var oldCookie = CookieUtils.createValidCookie(REFRESH_TOKEN_COOKIE_KEY, oldRefreshToken);
    request.setCookies(oldCookie);

    // when
    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
    when(cacheRepository.get(anyString())).thenReturn(oldRefreshToken);
    doNothing().when(cacheRepository).set(anyString(), anyString());

    TokenReissueResponse tokenResponse = authService.reissueToken(request, response);

    // then
    assertThat(tokenResponse.accessToken()).isNotNull();
    Cookie newCookie = response.getCookie(REFRESH_TOKEN_COOKIE_KEY);
    assertThat(newCookie).isNotNull();
    assertThat(newCookie).isNotSameAs(oldCookie);
  }

  @Test
  @DisplayName("만료된 리프레시 토큰에 대해 토큰 재발급 차단")
  @WithMockTodakUser
  void reissueTokenFailTest() {
    // given
    UtilConfig.expiredJwtUtilSetup();
    TodakUser user =
        (TodakUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    var oldRefreshToken = JwtUtils.issueToken(user, REFRESH_TYPE);
    var oldCookie = CookieUtils.createValidCookie(REFRESH_TOKEN_COOKIE_KEY, oldRefreshToken);
    request.setCookies(oldCookie);

    // when + then
    AuthException exception =
        assertThrows(AuthException.class, () -> authService.reissueToken(request, response));
    assertThat(exception.getErrorSpec()).isEqualTo(AuthErrorSpec.RE_LOGIN_REQUIRED);
  }
}
