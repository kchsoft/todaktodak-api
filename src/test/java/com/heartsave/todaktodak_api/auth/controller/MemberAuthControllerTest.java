package com.heartsave.todaktodak_api.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.auth.dto.request.LoginIdCheckRequest;
import com.heartsave.todaktodak_api.auth.dto.request.NicknameCheckRequest;
import com.heartsave.todaktodak_api.auth.dto.request.SignUpRequest;
import com.heartsave.todaktodak_api.auth.exception.AuthException;
import com.heartsave.todaktodak_api.auth.service.AuthService;
import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
import com.heartsave.todaktodak_api.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private AuthService authService;

  @Autowired ObjectMapper objectMapper;

  @Test
  @DisplayName("유일한 닉네임에 대한 중복 확인 요청")
  void checkNickname204Test() throws Exception {
    // given
    NicknameCheckRequest request = new NicknameCheckRequest("TEST_NICKNAME");

    // when
    when(authService.isDuplicatedNickname(any(String.class))).thenReturn(false);

    // then
    mockMvc
        .perform(
            post("/api/v1/auth/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(handler().handlerType(AuthController.class))
        .andExpect(status().isNoContent());

    // Mock 호출 확인
    verify(authService).isDuplicatedNickname(any(String.class));
  }

  @Test
  @DisplayName("유일한 로그인 아이디에 대한 중복 확인 요청")
  void checkLoginId204Test() throws Exception {
    // given
    LoginIdCheckRequest request = new LoginIdCheckRequest("TEST_LOGIN_ID");

    // when
    when(authService.isDuplicatedLoginId(any(String.class))).thenReturn(false);

    // then
    mockMvc
        .perform(
            post("/api/v1/auth/login-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNoContent());
    verify(authService, times(1)).isDuplicatedLoginId(any(String.class));
  }

  @Test
  @DisplayName("회원가입 성공")
  void signUp204Test() throws Exception {
    // given
    SignUpRequest request = new SignUpRequest("test@test.com", "unique", "todak", "todak!");
    doNothing().when(authService).signUp(any(SignUpRequest.class));

    // when + then
    mockMvc
        .perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("회원가입 실패 - 중복된 정보")
  void signUp409Test() throws Exception {
    // given
    SignUpRequest request = new SignUpRequest("test@test.com", "unique", "todak", "todak!");
    doThrow(new AuthException(AuthErrorSpec.DUPLICATED_INFORMATION, request))
        .when(authService)
        .signUp(any(SignUpRequest.class));

    // when + then
    mockMvc
        .perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("회원가입 실패 - 잘못된 입력값")
  void signUpInvalidation400Test() throws Exception {
    // given
    // 잘못된 이메일
    SignUpRequest request = new SignUpRequest("invalid-email", "unique", "todak", "todak!");

    // when & then
    mockMvc
        .perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
