package com.heartsave.todaktodak_api.common.security.component.oauth2;

import static com.heartsave.todaktodak_api.common.security.constant.Oauth2ErrorConstant.DUPLICATED_EMAIL_ERROR;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.ErrorResponse;
import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@RequiredArgsConstructor
public class Oauth2FailureHandler implements AuthenticationFailureHandler {
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType("application/json;charset=UTF-8");
    if (!(exception instanceof OAuth2AuthenticationException e)) return;
    setResponseBody(response, e);
  }

  private void setResponseBody(HttpServletResponse response, OAuth2AuthenticationException e)
      throws IOException {
    // 중복 이메일로 회원가입
    if (DUPLICATED_EMAIL_ERROR.getErrorCode().equals(e.getError().getErrorCode())) {
      response
          .getWriter()
          .write(
              objectMapper.writeValueAsString(
                  ErrorResponse.from(AuthErrorSpec.OAUTH_DUPLICATED_EMAIL)));
    }
    // 이외
    else {
      response
          .getWriter()
          .write(
              objectMapper.writeValueAsString(ErrorResponse.from(AuthErrorSpec.OAUTH_LOGIN_FAIL)));
    }
  }
}
