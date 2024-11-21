package com.heartsave.todaktodak_api.common.security.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.ErrorResponse;
import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

@RequiredArgsConstructor
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ObjectMapper objectMapper;

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    logger.error("인가 에러가 발생했습니다: {}", accessDeniedException.getMessage());
    response.setCharacterEncoding("UTF-8");
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response
        .getWriter()
        .write(
            objectMapper.writeValueAsString(
                ErrorResponse.from(AuthErrorSpec.TEMP_USER_DIARY_CREATE_FAIL)));
  }
}
