package com.heartsave.todaktodak_api.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.auth.dto.request.LoginIdCheckRequest;
import com.heartsave.todaktodak_api.auth.dto.response.NicknameCheckRequest;
import com.heartsave.todaktodak_api.auth.service.AuthService;
import com.heartsave.todaktodak_api.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.OncePerRequestFilter;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {OncePerRequestFilter.class})
    })
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
    when(authService.isDuplicatedNickname(any(NicknameCheckRequest.class))).thenReturn(false);

    // then
    mockMvc
        .perform(
            post("/api/v1/auth/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(handler().handlerType(AuthController.class))
        .andExpect(status().isNoContent());

    // Mock 호출 확인
    verify(authService).isDuplicatedNickname(any(NicknameCheckRequest.class));
  }

  @Test
  @DisplayName("유일한 로그인 아이디에 대한 중복 확인 요청")
  void checkLoginId204Test() throws Exception {
    // given
    LoginIdCheckRequest request = new LoginIdCheckRequest("TEST_LOGIN_ID");

    // when
    when(authService.isDuplicatedLoginId(any(LoginIdCheckRequest.class))).thenReturn(false);

    // then
    mockMvc
        .perform(
            post("/api/v1/auth/login-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andDo(print())
        .andExpect(status().isNoContent());
    verify(authService, times(1)).isDuplicatedLoginId(any(LoginIdCheckRequest.class));
  }
}
