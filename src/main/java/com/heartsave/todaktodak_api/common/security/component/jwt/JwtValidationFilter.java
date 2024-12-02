package com.heartsave.todaktodak_api.common.security.component.jwt;

import static com.heartsave.todaktodak_api.common.security.constant.JwtConstant.*;
import static com.heartsave.todaktodak_api.common.security.util.JwtUtils.*;

import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.TokenErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final AuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var token = extractToken(request);

    if (token == null) {
      setNoTokenRequestAttribute(request);
      filterChain.doFilter(request, response);
      return;
    }

    if (!validateTokenAndProcess(token, request, response)) return;
    setAuthentication(token);
    filterChain.doFilter(request, response);
  }

  @Nullable
  private String extractToken(HttpServletRequest request) {
    String value = request.getHeader(HEADER_KEY);
    if (value == null || !value.contains(TOKEN_PREFIX)) return null;
    return value.substring(TOKEN_PREFIX.length());
  }

  private void setNoTokenRequestAttribute(HttpServletRequest request) {
    if (existRefreshTokenCookie(request)) {
      request.setAttribute(NO_TOKEN_REQUEST_ATTRIBUTE_KEY, TokenErrorSpec.NON_EXISTENT_TOKEN);
    } else {
      request.setAttribute(NO_TOKEN_REQUEST_ATTRIBUTE_KEY, AuthErrorSpec.ABNORMAL_ACCESS);
    }
  }

  private boolean existRefreshTokenCookie(HttpServletRequest request) {
    return request.getCookies() != null
        && Arrays.stream(request.getCookies())
            .anyMatch(p -> p.getName().equals(REFRESH_TOKEN_COOKIE_KEY));
  }

  private boolean validateTokenAndProcess(
      String token, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {
      extractAllClaims(token);
      if (!isAccessTokenType(token)) {
        logger.error("유효하지 않은 토큰 유형입니다. {}", token);
        respondAuthError(request, response, TokenErrorSpec.INVALID_TOKEN.name());
        return false;
      }
      return true;
    } catch (ExpiredJwtException e) {
      logger.error("토큰이 만료됐습니다. {}", token);
      respondAuthError(request, response, TokenErrorSpec.EXPIRED_TOKEN.name());
    } catch (SecurityException
        | MalformedJwtException
        | UnsupportedJwtException
        | IllegalArgumentException e) {
      logger.error("유효하지 않은 토큰입니다. {}", token);
      respondAuthError(request, response, TokenErrorSpec.INVALID_TOKEN.name());
    } catch (Exception e) {
      logger.error("토큰 검증이 실패했습니다. {}", e.getMessage());
    }
    return false;
  }

  private boolean isAccessTokenType(String token) {
    return extractType(token).equals(ACCESS_TYPE);
  }

  private void respondAuthError(
      HttpServletRequest request, HttpServletResponse response, String errorMessage)
      throws ServletException, IOException {
    authenticationEntryPoint.commence(request, response, new BadCredentialsException(errorMessage));
  }

  private void setAuthentication(String token) {
    var authentication = getAuthentication(token);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    logger.info("인증 정보 구성 완료: {}", authentication.getPrincipal());
  }

  private Authentication getAuthentication(String token) {
    TodakUser user = extractUserFromToken(token);
    return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
  }

  private TodakUser extractUserFromToken(String token) {
    return TodakUser.builder()
        .id(extractSubject(token))
        .username(extractUsername(token))
        .role(extractRole(token))
        .build();
  }
}
