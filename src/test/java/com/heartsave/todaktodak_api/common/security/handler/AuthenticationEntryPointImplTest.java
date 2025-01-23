package com.heartsave.todaktodak_api.common.security.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.common.exception.ErrorResponse;
import com.heartsave.todaktodak_api.common.exception.errorspec.auth.AuthErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.auth.TokenErrorSpec;
import com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

final class AuthenticationEntryPointImplTest {
  private ObjectMapper objectMapper;
  private AuthenticationEntryPointImpl authenticationEntryPoint;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setup() {
    objectMapper = new ObjectMapper();
    authenticationEntryPoint = new AuthenticationEntryPointImpl(objectMapper);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  @DisplayName("인증이 필요하지만 토큰과 쿠키가 없는 경우 비정상 접근 시도로 간주")
  void abnormalAccess_noRefreshCookie() throws Exception {
    // given
    request.setAttribute(JwtConstant.NO_TOKEN_REQUEST_ATTRIBUTE_KEY, AuthErrorSpec.ABNORMAL_ACCESS);

    // when
    authenticationEntryPoint.commence(request, response, new BadCredentialsException(""));

    // then
    assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
    assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);

    ErrorResponse errorResponse =
        objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
    assertThat(errorResponse.getTitle()).isEqualTo(AuthErrorSpec.ABNORMAL_ACCESS.name());
  }

  @Test
  @DisplayName("인증이 필요하지만 토큰이 없고 쿠키가 있는 경우 토큰 재발급 필요로 간주")
  void unauthorized_nonExistentTokenWithCookie() throws Exception {
    // given
    request.setAttribute(
        JwtConstant.NO_TOKEN_REQUEST_ATTRIBUTE_KEY, TokenErrorSpec.NON_EXISTENT_TOKEN);

    // when
    authenticationEntryPoint.commence(request, response, new BadCredentialsException(""));

    // then
    ErrorResponse errorResponse =
        objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
    assertThat(errorResponse.getTitle()).isEqualTo(TokenErrorSpec.NON_EXISTENT_TOKEN.name());
  }

  @ParameterizedTest
  @DisplayName("토큰 유효성 검사 실패로 인한 응답")
  @MethodSource("tokenErrorSpecs")
  void unauthorized_invalidToken(TokenErrorSpec spec) throws Exception {
    // when
    authenticationEntryPoint.commence(request, response, new BadCredentialsException(spec.name()));

    // then
    ErrorResponse errorResponse =
        objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
    assertThat(errorResponse.getTitle()).isEqualTo(spec.name());
    assertThat(errorResponse.getMessage()).isEqualTo(spec.getClientMessage());
  }

  static Stream<TokenErrorSpec> tokenErrorSpecs() {
    return Stream.of(TokenErrorSpec.EXPIRED_TOKEN, TokenErrorSpec.INVALID_TOKEN);
  }
}
