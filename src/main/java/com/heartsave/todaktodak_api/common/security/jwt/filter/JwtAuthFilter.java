package com.heartsave.todaktodak_api.common.security.jwt.filter;

import static com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant.*;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.ErrorResponse;
import com.heartsave.todaktodak_api.common.exception.errorspec.auth.AuthErrorSpec;
import com.heartsave.todaktodak_api.common.security.cookie.CookieUtils;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.jwt.util.JwtUtils;
import com.heartsave.todaktodak_api.domain.auth.cache.RefreshTokenCache;
import com.heartsave.todaktodak_api.domain.auth.dto.request.LoginRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.response.LoginResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtAuthFilter extends UsernamePasswordAuthenticationFilter {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final AuthenticationManager authenticationManager;
  private final ObjectMapper objectMapper;
  private final RefreshTokenCache refreshCache;
  private static final String LOGIN_URL = "/api/v1/auth/login";

  public JwtAuthFilter(
      AuthenticationManager authenticationManager,
      ObjectMapper objectMapper,
      RefreshTokenCache refreshCache) {
    this.authenticationManager = authenticationManager;
    this.objectMapper = objectMapper;
    this.refreshCache = refreshCache;
    setFilterProcessesUrl(LOGIN_URL);
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    try {
      LoginRequest dto = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
      return authenticationManager.authenticate(createAuthToken(dto));
    } catch (IOException e) {
      logger.error("잘못된 요청 데이터 형식", e);
      throw new RuntimeException("잘못된 요청 데이터 형식", e);
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authentication)
      throws IOException {
    TodakUser user = (TodakUser) authentication.getPrincipal();
    var accessToken = JwtUtils.issueToken(user, ACCESS_TYPE);
    var refreshToken = JwtUtils.issueToken(user, REFRESH_TYPE);
    var refreshCookie = CookieUtils.createValidCookie(REFRESH_TOKEN_COOKIE_KEY, refreshToken);

    // 캐싱
    refreshCache.set(String.valueOf(user.getId()), refreshToken);

    // 응답
    CookieUtils.updateCookie(response, refreshCookie);
    LoginResponse dto = getResponseBody(authentication, accessToken);
    response.getWriter().write(objectMapper.writeValueAsString(dto));
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {
    logger.error("로그인 실패");
    response.setStatus(SC_UNAUTHORIZED);
    setResponseHeader(response);
    response
        .getWriter()
        .write(
            objectMapper.writeValueAsString(
                ErrorResponse.from(AuthErrorSpec.INCORRECT_USERNAME_PASSWORD)));
  }

  private UsernamePasswordAuthenticationToken createAuthToken(LoginRequest dto) {
    return new UsernamePasswordAuthenticationToken(dto.loginId(), dto.password());
  }

  private LoginResponse getResponseBody(Authentication authentication, String accessToken) {
    return LoginResponse.builder()
        .username(authentication.getName())
        .accessToken(accessToken)
        .build();
  }

  private void setResponseHeader(HttpServletResponse response) {
    response.setContentType(APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(UTF_8.name());
  }
}
