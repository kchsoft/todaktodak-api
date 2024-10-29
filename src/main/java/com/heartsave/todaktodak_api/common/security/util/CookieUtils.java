package com.heartsave.todaktodak_api.common.security.util;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
  private static Long MAX_AGE;

  @Value("${jwt.refresh-expire-time}")
  Long refreshTokenExpireTimeMilliSecond;

  @PostConstruct
  void init() {
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
