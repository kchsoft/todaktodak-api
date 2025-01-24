package com.heartsave.todaktodak_api.domain.auth.integrate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.jwt.util.JwtUtils;
import com.heartsave.todaktodak_api.config.BaseIntegrateTest;
import com.heartsave.todaktodak_api.config.BaseTestObject;
import com.heartsave.todaktodak_api.domain.auth.cache.RefreshTokenCache;
import com.heartsave.todaktodak_api.domain.auth.dto.request.LoginRequest;
import com.heartsave.todaktodak_api.domain.auth.dto.request.SignUpRequest;
import com.heartsave.todaktodak_api.domain.auth.exception.AuthException;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

public class JwtIntegrateTest extends BaseIntegrateTest {
  private MemberEntity member;

  @Autowired MemberRepository memberRepository;
  @Autowired RefreshTokenCache refreshTokenCache;

  @Value("${jwt.secret-key}")
  String jwtSecretKey;

  private byte[] keyBytes;
  private SecretKey secretKey;

  @BeforeEach
  void setup() throws Exception {

    // set member
    member = BaseTestObject.createMember();
    SignUpRequest request =
        new SignUpRequest(
            member.getEmail(), member.getNickname(), member.getLoginId(), member.getPassword());
    // signup
    mockMvc
        .perform(
            post("/api/v1/auth/signup")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  private String issueExpiredTokenBy(String type) {
    keyBytes = Decoders.BASE64.decode(jwtSecretKey);
    secretKey = Keys.hmacShaKeyFor(keyBytes);

    long now = System.currentTimeMillis();
    long expireTime = -1000L;
    return Jwts.builder()
        .subject(String.valueOf(member.getId()))
        .claim("username", member.getLoginId())
        .claim("role", member.getRole())
        .claim("type", type)
        .issuedAt(new Date(now))
        .expiration(new Date(now + expireTime))
        .signWith(secretKey)
        .compact();
  }

  @Nested
  @DisplayName("accessToken 테스트")
  class AccessToken_Test {
    @Test
    @DisplayName("accessToken 발급 성공")
    void accessToken_issue_success() throws Exception {
      // given
      LoginRequest request = new LoginRequest(member.getLoginId(), member.getPassword());

      // when
      MvcResult mvcResult =
          mockMvc
              .perform(post("/api/v1/auth/login").content(objectMapper.writeValueAsString(request)))
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

    @Test
    @DisplayName("만료된 accessToken 인증 실패")
    void expired_accessToken_auth_fail() throws Exception {
      String expiredAccessToken = issueExpiredTokenBy(JwtConstant.ACCESS_TYPE);

      // when
      mockMvc
          .perform(
              get("/api/v1/diary/public")
                  .header(JwtConstant.HEADER_KEY, JwtConstant.TOKEN_PREFIX + expiredAccessToken))
          .andExpect(status().isUnauthorized())
          .andDo(print());
    }
  }

  /*
   * 5. 만료된 access
   * 6. 만료된 refresh
   * */

  @Nested
  @DisplayName("refreshToken 테스트")
  class RefreshToken_Test {

    private String REFRESHTOKEN_REISSUE_URL = "/api/v1/auth/refresh-token";

    @Test
    @DisplayName("refreshToken 재발급 성공")
    void refreshToken_reissue_success() throws Exception {

      // given
      TodakUser todakUser =
          TodakUser.builder()
              .id(member.getId())
              .username(member.getLoginId())
              .password(member.getPassword())
              .role(member.getRole().name())
              .attributes(Map.of())
              .build();
      String oldRefreshToken = JwtUtils.issueToken(todakUser, JwtConstant.REFRESH_TYPE);
      when(refreshTokenCache.get(anyString())).thenReturn(oldRefreshToken);

      // when
      MvcResult mvcResult =
          mockMvc
              .perform(
                  post(REFRESHTOKEN_REISSUE_URL)
                      .cookie(new Cookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY, oldRefreshToken))
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
          .isNotEqualTo(oldRefreshToken);
    }

    @Test
    @DisplayName("유효하지 않은 refreshToken 재발급 실패")
    void invalid_refreshToken_reissue_fail() throws Exception {

      // given
      String invalidRefreshToken = "invalidRefreshToken";

      // when
      mockMvc
          .perform(
              post(REFRESHTOKEN_REISSUE_URL)
                  .cookie(new Cookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY, invalidRefreshToken)))
          .andExpect(status().isUnauthorized())
          .andExpect(
              result -> {
                Exception exception = result.getResolvedException();
                assertThat(exception).isInstanceOf(AuthException.class);
              })
          .andDo(print());
    }

    @Test
    @DisplayName("만료된 refreshToken 재발급 실패")
    void expired_refreshToken_reissue_fail() throws Exception {
      String expiredRefreshToken = issueExpiredTokenBy(JwtConstant.REFRESH_TYPE);

      // when
      mockMvc
          .perform(
              post(REFRESHTOKEN_REISSUE_URL)
                  .cookie(new Cookie(JwtConstant.REFRESH_TOKEN_COOKIE_KEY, expiredRefreshToken)))
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
