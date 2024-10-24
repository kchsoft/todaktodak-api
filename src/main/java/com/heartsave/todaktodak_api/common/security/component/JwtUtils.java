package com.heartsave.todaktodak_api.common.security.component;

import com.heartsave.todaktodak_api.common.security.constant.JwtConstant;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtils {

  private final String ROLE = "role";
  private final String TYPE = "type";
  private final String USERNAME = "username";

  @Value("${jwt.secret-key}")
  private String JWT_SECRET_KEY;

  @Value("${jwt.access-expire-time}")
  private Long ACCESS_TOKEN_EXPIRE_TIME_MILLI_SECOND;

  @Value("${jwt.refresh-expire-time}")
  private Long REFRESH_TOKEN_EXPIRE_TIME_MILLI_SECOND;

  private SecretKey key;

  public String issueToken(TodakUser user, String type) {
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

  private String extractRole(String token) {
    return extractAllClaims(token).get(ROLE, String.class);
  }

  public String extractType(String token) {
    return extractAllClaims(token).get(TYPE, String.class);
  }

  public String extractSubject(String token) {
    return extractAllClaims(token).getSubject();
  }

  public Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  @PostConstruct
  private void init() {
    initKey();
  }

  private void initKey() {
    var keyBytes = Decoders.BASE64.decode(JWT_SECRET_KEY);
    key = Keys.hmacShaKeyFor(keyBytes);
  }
}
