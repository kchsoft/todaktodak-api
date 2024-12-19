package com.heartsave.todaktodak_api.common.security.component.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.auth.repository.RefreshTokenCacheRepository;
import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.security.constant.JwtConstant;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

final class JwtLogoutFilterTest {
  private JwtLogoutFilter jwtLogoutFilter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private MockFilterChain filterChain;

  @BeforeEach
  void setup() {
    RefreshTokenCacheRepository refreshTokenCacheRepository =
        mock(RefreshTokenCacheRepository.class);
    jwtLogoutFilter = new JwtLogoutFilter(refreshTokenCacheRepository);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    filterChain = new MockFilterChain();
  }

  @Test
  @DisplayName("로그아웃 성공")
  @WithMockTodakUser
  void logoutSuccessTest() throws Exception {
    request.setMethod("POST");
    request.setRequestURI("/api/v1/auth/logout");
    request.setServletPath("/api/v1/auth/logout");

    jwtLogoutFilter.doFilter(request, response, filterChain);

    Cookie refreshTokenCookie = response.getCookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);
    assertThat(refreshTokenCookie).isNotNull();
    assertThat(refreshTokenCookie.getMaxAge()).isEqualTo(0);
    assertThat(refreshTokenCookie.getValue()).isNull();
    assertThat(refreshTokenCookie.isHttpOnly()).isTrue();
    assertThat(filterChain.getRequest()).isNull(); // 로그아웃 요청이므로 doFilter 미진행
  }

  @Test
  @DisplayName("로그아웃 URL이 아닌 경우 필터 무시")
  void ignoreNonLogoutUrlTest() throws Exception {
    request.setMethod("GET");
    request.setRequestURI("/api/v1/other");
    request.setServletPath("/api/v1/other");

    jwtLogoutFilter.doFilter(request, response, filterChain);

    assertThat(response.getCookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY)).isNull();
    assertThat(filterChain.getRequest()).isEqualTo(request); // 로그아웃이 아니므로 다음 filter로 진행
  }
}
