package com.heartsave.todaktodak_api.common.security.handler.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.ErrorResponse;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.jwt.filter.JwtAuthFilter;
import com.heartsave.todaktodak_api.common.security.util.UtilConfig;
import com.heartsave.todaktodak_api.domain.auth.cache.RefreshTokenCache;
import com.heartsave.todaktodak_api.domain.auth.dto.request.LoginRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.response.LoginResponse;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

final class JwtAuthFilterTest {
  private JwtAuthFilter jwtAuthFilter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private MockFilterChain filterChain;
  private AuthenticationManager authenticationManager;
  private ObjectMapper objectMapper;
  private RefreshTokenCache cacheRepository;

  @BeforeAll
  static void setupAll() {
    UtilConfig.utilSetup();
  }

  @BeforeEach
  void setup() {
    authenticationManager = mock(AuthenticationManager.class);
    objectMapper = new ObjectMapper();
    cacheRepository = mock(RefreshTokenCache.class);
    jwtAuthFilter = new JwtAuthFilter(authenticationManager, objectMapper, cacheRepository);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    filterChain = new MockFilterChain();

    String LOGIN_URL = "/api/v1/auth/login";
    request.setMethod("POST");
    request.setRequestURI(LOGIN_URL);
    request.setServletPath(LOGIN_URL);
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("로그인 성공")
  void loginSuccessTest() throws Exception {
    // given
    // request 설정
    LoginRequest loginRequest = new LoginRequest("testUser", "password");
    String content = objectMapper.writeValueAsString(loginRequest);
    request.setContent(content.getBytes(StandardCharsets.UTF_8));
    request.addHeader(
        "Content-Length", String.valueOf(content.getBytes(StandardCharsets.UTF_8).length));

    // LoginRequest와 일치하는 Authentication 토큰을 생성
    UsernamePasswordAuthenticationToken expectedAuthToken =
        new UsernamePasswordAuthenticationToken(loginRequest.loginId(), loginRequest.password());

    TodakUser todakUser = TodakUser.builder().id(1L).username("testUser").role("ROLE_USER").build();

    Authentication successAuthentication =
        new UsernamePasswordAuthenticationToken(todakUser, null, todakUser.getAuthorities());

    // LoginRequest로부터 생성될 Authentication 토큰과 정확히 매칭되도록 설정
    when(authenticationManager.authenticate(eq(expectedAuthToken)))
        .thenReturn(successAuthentication);
    doNothing().when(cacheRepository).set(anyString(), anyString());
    jwtAuthFilter.doFilter(request, response, filterChain);

    // then
    verify(authenticationManager).authenticate(any(Authentication.class));
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getCookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY)).isNotNull();
    assertThat(response.getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
    String responseBody = response.getContentAsString();
    LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);

    assertThat(loginResponse.accessToken()).isNotNull();
    assertThat(loginResponse.username()).isEqualTo("testUser");
    assertThat(loginResponse.accessToken()).isNotNull();
    assertThat(loginResponse.username()).isEqualTo("testUser");
  }

  @Test
  @DisplayName("로그인 실패")
  void loginFailTest() throws Exception {
    // given
    LoginRequest loginRequest = new LoginRequest("wrongUser", "wrongPassword");
    String content = objectMapper.writeValueAsString(loginRequest);
    request.setContent(content.getBytes(StandardCharsets.UTF_8));
    request.addHeader(
        "Content-Length", String.valueOf(content.getBytes(StandardCharsets.UTF_8).length));

    UsernamePasswordAuthenticationToken expectedAuthToken =
        new UsernamePasswordAuthenticationToken(loginRequest.loginId(), loginRequest.password());

    // when
    when(authenticationManager.authenticate(eq(expectedAuthToken)))
        .thenThrow(new BadCredentialsException("Invalid username or password"));
    jwtAuthFilter.doFilter(request, response, filterChain);

    // then
    verify(authenticationManager).authenticate(any(Authentication.class));
    assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(response.getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

    String responseBody = response.getContentAsString();
    ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);

    assertThat(errorResponse.getTitle()).isEqualTo("INCORRECT_USERNAME_PASSWORD");
    assertThat(errorResponse.getMessage()).isEqualTo("아이디 또는 비밀번호가 틀렸습니다.");
  }
}
