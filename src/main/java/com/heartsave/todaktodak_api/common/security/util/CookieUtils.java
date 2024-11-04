package com.heartsave.todaktodak_api.common.security.util;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
  private static Long MAX_AGE;

  private CookieUtils(@Value("${jwt.refresh-expire-time}") Long refreshTokenExpireTimeMilliSecond) {
    MAX_AGE = refreshTokenExpireTimeMilliSecond;
  }

  public static Cookie createValidCookie(String key, String value) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(MAX_AGE.intValue());
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    return cookie;
  }

  public static Cookie createExpiredCookie(String key) {
    Cookie cookie = new Cookie(key, null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    return cookie;
  }

  public static void updateCookie(HttpServletResponse response, Cookie cookie) {
    response.setContentType(APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(UTF_8.name());
    response.addCookie(cookie);
    response.setStatus(SC_OK);
  }

  @Nullable
  public static Cookie extractCookie(HttpServletRequest request, String cookieName) {
    if (request.getCookies() == null) {
      return null;
    }
    return Arrays.stream(request.getCookies())
        .filter(cookie -> cookie.getName().equals(cookieName))
        .findFirst()
        .orElse(null);
  }
}
