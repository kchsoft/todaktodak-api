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
  private static String JWT_SECRET_KEY;
  private static Long ACCESS_TOKEN_EXPIRE_TIME_MILLI_SECOND;
  private static Long REFRESH_TOKEN_EXPIRE_TIME_MILLI_SECOND;
  private static SecretKey key;

  private JwtUtils(
      @Value("${jwt.secret-key}") String JWT_SECRET_KEY,
      @Value("${jwt.access-expire-time}") Long ACCESS_TOKEN_EXPIRE_TIME_MILLI_SECOND,
      @Value("${jwt.refresh-expire-time}") Long REFRESH_TOKEN_EXPIRE_TIME_MILLI_SECOND) {
    var keyBytes = Decoders.BASE64.decode(JWT_SECRET_KEY);
    key = Keys.hmacShaKeyFor(keyBytes);
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

  private static String extractRole(String token) {
    return extractAllClaims(token).get(ROLE, String.class);
  }

  public static String extractType(String token) {
    return extractAllClaims(token).get(TYPE, String.class);
  }

  public static String extractSubject(String token) {
    return extractAllClaims(token).getSubject();
  }

  public static Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
