package com.heartsave.todaktodak_api.common.security.component.jwt;

import com.heartsave.todaktodak_api.common.security.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.util.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtLogoutFilter extends OncePerRequestFilter {
  private static final String LOGOUT_URL = "/api/v1/auth/logout";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (shouldNotFilter(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    var expiredCookie = CookieUtils.createExpiredCookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY);
    response.addCookie(expiredCookie);
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  // 로그아웃 요청이 아닌 경우 무시
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getServletPath().equals(LOGOUT_URL);
  }

  private Cookie extractRefreshTokenCookie(HttpServletRequest request) {
    return CookieUtils.extractCookie(request, JwtConstant.REFRESH_TOKEN_COOKIE_KEY);
  }
}
