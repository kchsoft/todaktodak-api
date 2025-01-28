package com.heartsave.todaktodak_api.config.config;

import com.heartsave.todaktodak_api.common.security.cookie.CookieUtils;
import com.heartsave.todaktodak_api.common.security.jwt.util.JwtUtils;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtUtilTestConfig {
  public static void utilSetup() {
    jwtUtilSetup();
    cookieUtilSetup();
  }

  public static void jwtUtilSetup() {
    ReflectionTestUtils.setField(JwtUtils.class, "ACCESS_TOKEN_EXPIRE_TIME_MILLI_SECOND", 10000L);
    ReflectionTestUtils.setField(JwtUtils.class, "REFRESH_TOKEN_EXPIRE_TIME_MILLI_SECOND", 100000L);
    ReflectionTestUtils.setField(
        JwtUtils.class,
        "key",
        Keys.hmacShaKeyFor(
            Decoders.BASE64.decode(
                "secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey")));
  }

  public static void expiredJwtUtilSetup() {
    jwtUtilSetup();
    ReflectionTestUtils.setField(JwtUtils.class, "ACCESS_TOKEN_EXPIRE_TIME_MILLI_SECOND", 0L);
    ReflectionTestUtils.setField(JwtUtils.class, "REFRESH_TOKEN_EXPIRE_TIME_MILLI_SECOND", 0L);
  }

  public static void cookieUtilSetup() {
    // CookieUtils 초기화
    ReflectionTestUtils.setField(CookieUtils.class, "MAX_AGE", 10000L);
  }
}
