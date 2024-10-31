package com.heartsave.todaktodak_api.common.security.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.ErrorResponse;
import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.TokenErrorSpec;
import com.heartsave.todaktodak_api.common.security.constant.JwtConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    setResponseWithMessage(response, authException);
  }

  private void setResponseWithMessage(HttpServletResponse response, AuthenticationException e)
      throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // 비정상 인증 토큰
    if (e.getMessage().equals(JwtConstant.INVALID_TOKEN_ERROR_DETAIL)) {
      response
          .getWriter()
          .write(objectMapper.writeValueAsString(ErrorResponse.from(TokenErrorSpec.INVALID_TOKEN)));
    }
    // 기타
    response
        .getWriter()
        .write(objectMapper.writeValueAsString(ErrorResponse.from(AuthErrorSpec.AUTH_FAIL)));
  }
}
