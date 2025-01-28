package com.heartsave.todaktodak_api.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.security.jwt.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.jwt.util.JwtUtils;
import com.heartsave.todaktodak_api.domain.member.domain.TodakRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
final class JwtUtilsTest {

  private static final String TEST_JWT_KEY =
      "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijkl";
  private static final Long TEST_ACCESS_EXPIRE_TIME = 10000L;
  private static final Long TEST_REFRESH_EXPIRE_TIME = 100000L;
  private static TodakUser user;

  @BeforeAll
  static void setupAll() {
    try {
      setupUtilConstructor();
    } catch (Exception ignored) {
    }
    user = mock(TodakUser.class);
    when(user.getId()).thenReturn(1L);
    when(user.getUsername()).thenReturn("todak");
    when(user.getRole()).thenReturn(TodakRole.ROLE_USER.name());
  }

  private static void setupUtilConstructor() throws Exception {
    Constructor<JwtUtils> constructor =
        JwtUtils.class.getDeclaredConstructor(String.class, Long.class, Long.class);
    constructor.setAccessible(true);
    constructor.newInstance(TEST_JWT_KEY, TEST_ACCESS_EXPIRE_TIME, TEST_REFRESH_EXPIRE_TIME);
  }

  @Test
  @DisplayName("발급한 액세스 토큰의 페이로드 확인")
  void issueAccessToken_extractClaimTest() {
    // given
    String token = JwtUtils.issueToken(user, JwtConstant.ACCESS_TYPE);

    // then
    assertThat(token).isNotNull();
    assertThat(JwtUtils.extractSubject(token)).isEqualTo(1L);
    assertThat(JwtUtils.extractUsername(token)).isEqualTo("todak");
    assertThat(JwtUtils.extractRole(token)).isEqualTo(TodakRole.ROLE_USER.name());
    assertThat(JwtUtils.extractType(token)).isEqualTo(JwtConstant.ACCESS_TYPE);
  }

  @Test
  @DisplayName("발급한 리프레시 토큰의 페이로드 확인")
  void issueRefreshToken_extractAllClaimsTest() throws Exception {
    // reflection
    Field typeField = JwtUtils.class.getDeclaredField("TYPE");
    Field roleField = JwtUtils.class.getDeclaredField("ROLE");
    Field usernameField = JwtUtils.class.getDeclaredField("USERNAME");

    typeField.setAccessible(true);
    roleField.setAccessible(true);
    usernameField.setAccessible(true);

    String TYPE = (String) typeField.get(null);
    String ROLE = (String) roleField.get(null);
    String USERNAME = (String) usernameField.get(null);
    // given
    String token = JwtUtils.issueToken(user, JwtConstant.REFRESH_TYPE);

    // when
    Claims claims = JwtUtils.extractAllClaims(token);

    // then
    assertThat(claims).isNotNull();
    assertThat(claims.getSubject()).isEqualTo("1");
    assertThat(claims.get(USERNAME)).isEqualTo("todak");
    assertThat(claims.get(ROLE)).isEqualTo(TodakRole.ROLE_USER.name());
    assertThat(claims.get(TYPE)).isEqualTo(JwtConstant.REFRESH_TYPE);
  }

  @Test
  @DisplayName("유효하지 않은 토큰의 페이로드 추출 시 예외 발생")
  void extractAllClaims_invalidTokenTest() {
    // given
    String invalidToken = "invalid.token.string";

    // when + then
    assertThatThrownBy(() -> JwtUtils.extractAllClaims(invalidToken))
        .isInstanceOf(JwtException.class);
  }

  @Test
  @DisplayName("만료된 토큰의 페이로드 추출 시 예외 발생")
  void extractAllClaims_expiredTokenTest() throws Exception {
    // given
    Constructor<JwtUtils> constructor =
        JwtUtils.class.getDeclaredConstructor(String.class, Long.class, Long.class);
    constructor.setAccessible(true);
    constructor.newInstance(TEST_JWT_KEY, 0L, 0L);

    String expiredToken = JwtUtils.issueToken(user, JwtConstant.ACCESS_TYPE);

    // when + then
    assertThatThrownBy(() -> JwtUtils.extractAllClaims(expiredToken))
        .isInstanceOf(ExpiredJwtException.class);
  }
}
