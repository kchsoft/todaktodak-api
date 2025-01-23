package com.heartsave.todaktodak_api.common.security.oauth2.handler;

import static com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant.*;

import com.heartsave.todaktodak_api.common.security.cookie.CookieUtils;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.jwt.util.JwtUtils;
import com.heartsave.todaktodak_api.domain.auth.cache.RefreshTokenCache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final RefreshTokenCache cacheRepository;

  @Value("${client.server.origin}")
  private String CLIENT_SERVER_ORIGIN;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    logger.info("OAUTH2 로그인 성공: {}", authentication.getPrincipal());
    String refreshToken =
        JwtUtils.issueToken((TodakUser) authentication.getPrincipal(), REFRESH_TYPE);
    cacheRepository.set(
        String.valueOf(((TodakUser) authentication.getPrincipal()).getId()), refreshToken);
    response.addCookie(CookieUtils.createValidCookie(REFRESH_TOKEN_COOKIE_KEY, refreshToken));
    response.sendRedirect(CLIENT_SERVER_ORIGIN + "/home");
  }
}
