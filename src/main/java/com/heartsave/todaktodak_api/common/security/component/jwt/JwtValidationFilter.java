package com.heartsave.todaktodak_api.common.security.component.jwt;

import static com.heartsave.todaktodak_api.common.security.constant.JwtConstant.*;
import static com.heartsave.todaktodak_api.common.security.util.JwtUtils.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.errorspec.TokenErrorSpec;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {
  private final ObjectMapper objectMapper;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final AuthenticationEntryPoint authenticationEntryPoint;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var token = extractToken(request);

    if (token == null) {
      logger.error("토큰이 없습니다");
      filterChain.doFilter(request, response);
      return;
    }
    try {
      JwtUtils.extractAllClaims(token);
      if (!isValidTokenType(token)) {
        logger.error("유효하지 않은 토큰 유형입니다. {}", token);
        authenticationEntryPoint.commence(
            request, response, new BadCredentialsException(TokenErrorSpec.INVALID_TOKEN.name()));
      } else {
        setAuthentication(token);
        filterChain.doFilter(request, response);
      }
    } catch (ExpiredJwtException e) {
      logger.error("토큰이 만료됐습니다. {}", token);
      authenticationEntryPoint.commence(
          request, response, new BadCredentialsException(TokenErrorSpec.EXPIRED_TOKEN.name()));
    } catch (SecurityException
        | MalformedJwtException
        | UnsupportedJwtException
        | IllegalArgumentException e) {
      logger.error("유효하지 않은 토큰입니다. {}", token);
      authenticationEntryPoint.commence(
          request, response, new BadCredentialsException(TokenErrorSpec.INVALID_TOKEN.name()));
    }
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
    logger.info("인증 정보 구성 완료: {}", authentication);
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
