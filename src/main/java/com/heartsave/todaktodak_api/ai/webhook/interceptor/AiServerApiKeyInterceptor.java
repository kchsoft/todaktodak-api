package com.heartsave.todaktodak_api.ai.webhook.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.ErrorResponse;
import com.heartsave.todaktodak_api.common.exception.errorspec.AiErrorSpec;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
@Component
public class AiServerApiKeyInterceptor implements HandlerInterceptor {

  @Value("${ai.server.api.key}")
  private String apiKey;

  private final ObjectMapper objectMapper;
  private final String API_KEY_HEADER = "X-API-KEY";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    log.info("AI 서버의 요청이 들어와 API-KEY를 검증합니다.");
    String apiKey = request.getHeader(API_KEY_HEADER);
    if (apiKey == null || !apiKey.equals(this.apiKey)) {
      setUnauthorized(response);
      printWarnLog(request);
      return false;
    }
    log.info("AI 서버의 API-KEY 검증을 성공적으로 완료하였습니다.");
    return true;
  }

  private void setUnauthorized(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    ErrorResponse error = ErrorResponse.from(AiErrorSpec.INVALID_API_KEY);
    response.getWriter().write(objectMapper.writeValueAsString(error));
  }

  private void printWarnLog(HttpServletRequest request) {
    log.warn(
        "AI 서버가 요청한 API KEY가 유효하지 않습니다. URI={}, IP={}",
        request.getRequestURI(),
        request.getRemoteAddr());
  }
}
