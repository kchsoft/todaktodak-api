package com.heartsave.todaktodak_api.common.security.component.jwt;

import static com.heartsave.todaktodak_api.common.security.constant.JwtConstant.*;
import static com.heartsave.todaktodak_api.common.security.util.JwtUtils.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.util.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.security.sasl.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {
  private final ObjectMapper objectMapper;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final String LOGIN_URL = "/api/v1/auth/login";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var token = extractToken(request);

    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }
    try {
      JwtUtils.extractAllClaims(token);
      if (!isValidTokenType(token)) {
        logger.error("유효하지 않은 토큰 유형입니다. {}", token);
        throw new AuthenticationException(INVALID_TOKEN_ERROR_DETAIL);
      }
      setAuthentication(token);
    } catch (ExpiredJwtException e) {
      logger.error("토큰이 만료됐습니다. {}", token);
    } catch (SecurityException
        | MalformedJwtException
        | UnsupportedJwtException
        | IllegalArgumentException e) {
      logger.error("유효하지 않은 토큰입니다. {}", token);
    }
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return request.getServletPath().equals(LOGIN_URL);
  }

  private String extractToken(HttpServletRequest request) {
    String value = request.getHeader(HEADER_KEY);
    if (value == null || !value.contains(TOKEN_PREFIX)) return null;
    return value.substring(TOKEN_PREFIX.length());
  }

  private boolean isValidTokenType(String token) {
    return JwtUtils.extractType(token).equals(ACCESS_TYPE);
  }

  private void setAuthentication(String token) {
    var authentication = getAuthentication(token);
    SecurityContextHolder.getContext().setAuthentication(authentication);
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
