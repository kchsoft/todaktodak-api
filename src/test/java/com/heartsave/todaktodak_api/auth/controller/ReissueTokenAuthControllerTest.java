package com.heartsave.todaktodak_api.auth.controller;

import static com.heartsave.todaktodak_api.common.security.constant.JwtConstant.ACCESS_TYPE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.heartsave.todaktodak_api.auth.dto.response.TokenReissueResponse;
import com.heartsave.todaktodak_api.auth.exception.AuthException;
import com.heartsave.todaktodak_api.auth.service.AuthService;
import com.heartsave.todaktodak_api.common.exception.errorspec.AuthErrorSpec;
import com.heartsave.todaktodak_api.common.security.WithMockTodakUser;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.util.JwtUtils;
import com.heartsave.todaktodak_api.common.security.util.UtilConfig;
import com.heartsave.todaktodak_api.config.TestSecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
public class ReissueTokenAuthControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean private AuthService authService;

  @BeforeEach
  void setup() {
    UtilConfig.utilSetup();
  }

  @Test
  @DisplayName("토큰 재발급 성공")
  @WithMockTodakUser
  void reissueTokenSuccessTest() throws Exception {
    // given
    TodakUser user =
        (TodakUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String newAccessToken = JwtUtils.issueToken(user, ACCESS_TYPE);
    TokenReissueResponse response =
        TokenReissueResponse.builder().accessToken(newAccessToken).build();

    when(authService.reissueToken(any(HttpServletRequest.class), any(HttpServletResponse.class)))
        .thenReturn(response);

    // when + then
    mockMvc
        .perform(post("/api/v1/auth/refresh-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value(newAccessToken))
        .andDo(print());
  }

  @Test
  @DisplayName("토큰 재발급 실패 - 만료된 토큰")
  @WithMockTodakUser
  void reissueTokenFailTest() throws Exception {
    // given
    when(authService.reissueToken(any(), any()))
        .thenThrow(new AuthException(AuthErrorSpec.RE_LOGIN_REQUIRED));

    // when + then
    mockMvc
        .perform(post("/api/v1/auth/refresh-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.title").value(AuthErrorSpec.RE_LOGIN_REQUIRED.name()))
        .andExpect(jsonPath("$.message").value(AuthErrorSpec.RE_LOGIN_REQUIRED.getClientMessage()))
        .andDo(print());
  }
}
