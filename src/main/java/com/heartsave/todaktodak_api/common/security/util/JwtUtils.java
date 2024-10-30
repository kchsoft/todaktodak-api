package com.heartsave.todaktodak_api.common.security.util;

import com.heartsave.todaktodak_api.common.security.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {

  private static final String ROLE = "role";
  private static final String TYPE = "type";
  private static final String USERNAME = "username";
  private static Long ACCESS_TOKEN_EXPIRE_TIME_MILLI_SECOND;
  private static Long REFRESH_TOKEN_EXPIRE_TIME_MILLI_SECOND;
  private static SecretKey key;

  private JwtUtils(
      @Value("${jwt.secret-key}") String jwtSecretKey,
      @Value("${jwt.access-expire-time}") Long accessTokenExpireTimeMilliSecond,
      @Value("${jwt.refresh-expire-time}") Long refreshTokenExpireTimeMilliSecond) {
    var keyBytes = Decoders.BASE64.decode(jwtSecretKey);
    key = Keys.hmacShaKeyFor(keyBytes);
    ACCESS_TOKEN_EXPIRE_TIME_MILLI_SECOND = accessTokenExpireTimeMilliSecond;
    REFRESH_TOKEN_EXPIRE_TIME_MILLI_SECOND = refreshTokenExpireTimeMilliSecond;
  }

  public static String issueToken(TodakUser user, String type) {
    long now = System.currentTimeMillis();
    long expireTime =
        type.equals(JwtConstant.ACCESS_TYPE)
            ? ACCESS_TOKEN_EXPIRE_TIME_MILLI_SECOND
            : REFRESH_TOKEN_EXPIRE_TIME_MILLI_SECOND;
    return Jwts.builder()
        .subject(String.valueOf(user.getId()))
        .claim(USERNAME, user.getUsername())
        .claim(ROLE, user.getRole())
        .claim(TYPE, type)
        .issuedAt(new Date(now))
        .expiration(new Date(now + expireTime))
        .signWith(key)
        .compact();
  }

  public static String extractRole(String token) {
    return extractAllClaims(token).get(ROLE, String.class);
  }

  public static String extractUsername(String token) {
    return extractAllClaims(token).get(USERNAME, String.class);
  }

  public static String extractType(String token) {
    return extractAllClaims(token).get(TYPE, String.class);
  }

  public static Long extractSubject(String token) {
    return Long.valueOf(extractAllClaims(token).getSubject());
  }

  public static Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
