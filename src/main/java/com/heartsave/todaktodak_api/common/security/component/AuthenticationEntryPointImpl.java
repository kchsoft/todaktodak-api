package com.heartsave.todaktodak_api.common.security.component;

import static com.heartsave.todaktodak_api.common.security.constant.JwtConstant.NO_TOKEN_REQUEST_ATTRIBUTE_KEY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.auth.exception.AuthException;
import com.heartsave.todaktodak_api.common.exception.ErrorResponse;
import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.TokenErrorSpec;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    logger.error(
        "인증이 실패했어요. 예외 타입={}, 예외 메시지={}", authException.getClass(), authException.getMessage());
    setResponseWithMessage(request, response, authException);
  }

  private void setResponseWithMessage(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
      throws IOException {
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // 토큰이 없는 요청에 대한 응답
    if (isNoTokenRequest(request)) {
      response
          .getWriter()
          .write(
              objectMapper.writeValueAsString(
                  ErrorResponse.from(
                      (ErrorSpec) request.getAttribute(NO_TOKEN_REQUEST_ATTRIBUTE_KEY))));
      return;
    }

    // 토큰 유효성 검사 실패에 대한 응답
    try {
      TokenErrorSpec spec = TokenErrorSpec.valueOf(e.getMessage());
      response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.from(spec)));
    } catch (AuthException ex) {
      response
          .getWriter()
          .write(
              objectMapper.writeValueAsString(
                  ErrorResponse.from(AuthErrorSpec.valueOf(e.getMessage()))));
    } catch (Exception ex) {
      logger.error("예기치 못한 인증 에러 발생: {}", e.getMessage());
      response
          .getWriter()
          .write(objectMapper.writeValueAsString(ErrorResponse.from(AuthErrorSpec.AUTH_FAIL)));
    }
  }

  private boolean isNoTokenRequest(HttpServletRequest request) {
    return request.getAttribute(NO_TOKEN_REQUEST_ATTRIBUTE_KEY) != null;
  }
}
