package com.heartsave.todaktodak_api.common.security.cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
final class CookieUtilsTest {
  private static final String TEST_COOKIE_NAME = "test";
  private static final String TEST_COOKIE_VALUE = "jwt token";
  private static final Long REFRESH_TOKEN_EXPIRE_TIME = 3600L;

  @BeforeAll
  static void setup() {
    try {
      setupUtilConstructor();
    } catch (Exception ignored) {
    }
  }

  private static void setupUtilConstructor() throws Exception {
    // given
    Constructor<CookieUtils> constructor = CookieUtils.class.getDeclaredConstructor(Long.class);
    constructor.setAccessible(true);
    constructor.newInstance(REFRESH_TOKEN_EXPIRE_TIME);
  }

  @Test
  @DisplayName("유효한 쿠키 생성")
  void createValidCookieTest() {
    // when
    Cookie cookie = CookieUtils.createValidCookie(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);

    // then
    assertAll(
        () -> assertEquals(TEST_COOKIE_NAME, cookie.getName()),
        () -> assertEquals(TEST_COOKIE_VALUE, cookie.getValue()),
        () -> assertEquals(REFRESH_TOKEN_EXPIRE_TIME.intValue(), cookie.getMaxAge()),
        () -> assertEquals("/", cookie.getPath()),
        () -> assertTrue(cookie.isHttpOnly()));
  }

  @Test
  @DisplayName("만료된 쿠키 생성")
  void createExpiredCookieTest() {
    // when
    Cookie cookie = CookieUtils.createExpiredCookie(TEST_COOKIE_NAME);

    // then
    assertTrue(cookie.isHttpOnly());
  }

  @Test
  @DisplayName("쿠키 추출 - 존재하는 쿠키")
  void extractExistedCookieTest() {
    // given
    HttpServletRequest request = mock(HttpServletRequest.class);
    Cookie expectedCookie = new Cookie(TEST_COOKIE_NAME, TEST_COOKIE_VALUE);
    Cookie[] cookies = {
      new Cookie("other", "otherValue"), expectedCookie,
    };
    when(request.getCookies()).thenReturn(cookies);

    // when
    Cookie extractedCookie = CookieUtils.extractCookie(request, TEST_COOKIE_NAME);

    // then
    assertNotNull(extractedCookie);
    assertEquals(TEST_COOKIE_NAME, extractedCookie.getName());
    assertEquals(TEST_COOKIE_VALUE, extractedCookie.getValue());
  }

  @Test
  @DisplayName("쿠키 추출 - 존재하지 않는 쿠키")
  void extractCookieNullTest() {
    // given
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getCookies()).thenReturn(null);

    // when
    Cookie extractedCookie = CookieUtils.extractCookie(request, TEST_COOKIE_NAME);

    // then
    assertNull(extractedCookie);
  }
}
