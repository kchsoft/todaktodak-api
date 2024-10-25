package com.heartsave.todaktodak_api.common.security.util;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
  private static Long MAX_AGE;

  private CookieUtils(@Value("${jwt.refresh-expire-time}") Long MAX_AGE) {}

  public static Cookie createValidCookie(String key, String value) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(MAX_AGE.intValue());
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    return cookie;
  }

  public static Cookie createExpiredCookie(String key, String value) {
    Cookie cookie = new Cookie(key, value);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    return cookie;
  }
}
