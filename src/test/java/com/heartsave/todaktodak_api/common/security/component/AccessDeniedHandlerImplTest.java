package com.heartsave.todaktodak_api.common.security.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.ErrorResponse;
import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

final class AccessDeniedHandlerImplTest {
  private AccessDeniedHandlerImpl accessDeniedHandler;
  private ObjectMapper objectMapper;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setup() {
    objectMapper = new ObjectMapper();
    accessDeniedHandler = new AccessDeniedHandlerImpl(objectMapper);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  @DisplayName("권한이 없는 사용자의 인가 에러 처리")
  void forbiddenResponseTest() throws Exception {
    // when
    accessDeniedHandler.handle(request, response, new AccessDeniedException(""));

    // then
    assertThat(response.getContentType()).contains(StandardCharsets.UTF_8.name());
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    ErrorResponse errorResponse =
        objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
    assertThat(errorResponse.getTitle())
        .isEqualTo(AuthErrorSpec.TEMP_USER_DIARY_CREATE_FAIL.name());
    assertThat(errorResponse.getMessage())
        .isEqualTo(AuthErrorSpec.TEMP_USER_DIARY_CREATE_FAIL.getClientMessage());
  }
}
