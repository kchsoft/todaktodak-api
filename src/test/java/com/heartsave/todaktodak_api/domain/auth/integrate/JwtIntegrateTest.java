package com.heartsave.todaktodak_api.domain.auth.integrate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant;
import com.heartsave.todaktodak_api.config.integrate.BaseIntegrateTest;
import com.heartsave.todaktodak_api.domain.auth.cache.RefreshTokenCache;
import com.heartsave.todaktodak_api.domain.auth.dto.request.LoginRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.request.SignUpRequest;
import com.heartsave.todaktodak_api.domain.auth.exception.AuthException;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

public class JwtIntegrateTest extends BaseIntegrateTest {

  @Autowired MemberRepository memberRepository;
  @Autowired RefreshTokenCache refreshTokenCache;

  private String testEmail = "jwt-test-email@kakaco.com";
  private String testNickname = "jwt-test-nickname";
  private String testLoginId = "jwt-test-loginId";
  private String testPassword = "jwt-test-password";

  private String LOGIN_URL = "/api/v1/auth/login";
  private String SIGNUP_URL = "/api/v1/auth/signup";

  @BeforeEach
  void setup() throws Exception {
    // set signup request
    SignUpRequest request = new SignUpRequest(testEmail, testNickname, testLoginId, testPassword);

    // signup
    mockMvc
        .perform(
            post(SIGNUP_URL)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Nested
  @DisplayName("accessToken 테스트")
  class AccessToken_Test {

    @Test
    @DisplayName("accessToken 발급 성공")
    void accessToken_issue_success() throws Exception {
      // given
      LoginRequest request = new LoginRequest(testLoginId, testPassword);

      // when
      MvcResult mvcResult =
          mockMvc
              .perform(post(LOGIN_URL).content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.accessToken").exists())
              .andDo(print())
              .andReturn();
      Cookie cookie = mvcResult.getResponse().getCookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY);

      // then
      assertThat(cookie).as("cookie is null").isNotNull();
      assertThat(cookie.getValue()).as("refresh token is null").isNotNull();
    }

    @Test
    @DisplayName("유효하지 않은 accessToken 인증 실패")
    void invalid_accessToken_auth_fail() throws Exception {

      // given
      String invalidAccessToken = "invalidAccessToken";

      // when
      mockMvc
          .perform(
              get("/api/v1/diary/public")
                  .header(JwtConstant.HEADER_KEY, JwtConstant.TOKEN_PREFIX + invalidAccessToken))
          .andExpect(status().isUnauthorized())
          .andDo(print());
    }
  }

  @Nested
  @DisplayName("refreshToken 테스트")
  class RefreshToken_Test {

    private String REFRESH_TOKEN_REISSUE_URL = "/api/v1/auth/refresh-token";

    @Test
    @DisplayName("refreshToken 재발급 성공")
    void refreshToken_reissue_success() throws Exception {

      LoginRequest request = new LoginRequest(testLoginId, testPassword);
      MvcResult loginResult =
          mockMvc
              .perform(post(LOGIN_URL).content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isOk())
              .andDo(print())
              .andReturn();
      MockHttpServletResponse loginResponse = loginResult.getResponse();
      Cookie refreshCookie = loginResponse.getCookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY);
      assertThat(refreshCookie).as("refreshToken Cookie is null").isNotNull();
      String loginRefresh = refreshCookie.getValue();
      assertThat(loginRefresh).as("refreshToken is blank").isNotBlank().isNotNull();
      when(refreshTokenCache.get(anyString())).thenReturn(loginRefresh);

      // when
      MvcResult mvcResult =
          mockMvc
              .perform(
                  post(REFRESH_TOKEN_REISSUE_URL)
                      .cookie(new Cookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY, loginRefresh))
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andDo(print())
              .andReturn();
      Cookie cookie = mvcResult.getResponse().getCookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY);

      // then
      assertThat(cookie).as("cookie is null").isNotNull();
      assertThat(cookie.getValue()).as("refresh token is null").isNotNull();
      assertThat(cookie.getValue())
          .as("new refreshToken is same about old")
          .isNotEqualTo(loginRefresh);
    }

    @Test
    @DisplayName("유효하지 않은 refreshToken 재발급 실패")
    void invalid_refreshToken_reissue_fail() throws Exception {

      // given
      String invalidRefreshToken = "invalidRefreshToken";

      // when
      mockMvc
          .perform(
              post(REFRESH_TOKEN_REISSUE_URL)
                  .cookie(new Cookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY, invalidRefreshToken)))
          .andExpect(status().isUnauthorized())
          .andExpect(
              result -> {
                Exception exception = result.getResolvedException();
                assertThat(exception).isInstanceOf(AuthException.class);
              })
          .andDo(print());
    }
  }
}
