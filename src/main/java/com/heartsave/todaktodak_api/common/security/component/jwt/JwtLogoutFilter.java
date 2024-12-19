package com.heartsave.todaktodak_api.common.security.component.jwt;

import com.heartsave.todaktodak_api.auth.repository.RefreshTokenCacheRepository;
import com.heartsave.todaktodak_api.common.security.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.util.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtLogoutFilter extends OncePerRequestFilter {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private static final String LOGOUT_URL = "/api/v1/auth/logout";
  private final RefreshTokenCacheRepository tokenCacheRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (shouldNotFilter(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    clearRefreshToken();
    var expiredCookie = CookieUtils.createExpiredCookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY);
    response.addCookie(expiredCookie);
    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  // 로그아웃 요청이 아닌 경우 무시
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getServletPath().equals(LOGOUT_URL);
  }

  private void clearRefreshToken() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    logger.info("인증 정보 = {}", authentication);
    if (authentication == null) return;
    TodakUser user = (TodakUser) authentication.getPrincipal();
    tokenCacheRepository.delete(String.valueOf(user.getId()));
  }
}
