package com.heartsave.todaktodak_api.common.security.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.ErrorResponse;
import com.heartsave.todaktodak_api.common.exception.ErrorSpec;
import com.heartsave.todaktodak_api.common.security.constant.JwtConstant;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// TODO: JWT 필터 완성시 컴포넌트화
// @Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {
  private final JwtUtils jwtUtils;
  private final ObjectMapper objectMapper;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var token = extractToken(request);

    if (!isValidToken(token)) {
      setErrorResponse(response);
      return;
    }

    setAuthentication(token);
    filterChain.doFilter(request, response);
  }

  private void setErrorResponse(HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType("application/json;charset=UTF-8");
    response
        .getWriter()
        .write(objectMapper.writeValueAsString(ErrorResponse.from(ErrorSpec.INVALID_TOKEN)));
  }

  private String extractToken(HttpServletRequest request) {
    String value = request.getHeader(JwtConstant.HEADER_KEY);
    if (value == null || !value.contains(JwtConstant.TOKEN_PREFIX)) return null;
    return value.substring(JwtConstant.TOKEN_PREFIX.length());
  }

  private boolean isValidToken(String token) {
    return token != null && isValidTokenStructure(token) && isValidTokenType(token);
  }

  private boolean isValidTokenStructure(String token) {
    try {
      jwtUtils.extractAllClaims(token);
      return true;
    } catch (SecurityException e) {
      logger.error("서명이 유효하지 않습니다.");
    } catch (MalformedJwtException e) {
      logger.error("잘못된 JWT 형식입니다.");
    } catch (UnsupportedJwtException e) {
      logger.error("지원하지 않는 JWT입니다.");
    } catch (ExpiredJwtException e) {
      logger.error("토큰이 만료됐습니다.");
    } catch (IllegalArgumentException e) {
      logger.error("토큰이 비었거나 존재하지 않습니다.");
    }
    return false;
  }

  private boolean isValidTokenType(String token) {
    return jwtUtils.extractType(token).equals(JwtConstant.ACCESS_TYPE);
  }

  private void setAuthentication(String token) {
    var authentication = jwtUtils.getAuthentication(token);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
