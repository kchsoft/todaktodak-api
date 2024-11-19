package com.heartsave.todaktodak_api.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCacheRepository {
  @Value("${jwt.refresh-expire-time}")
  private Long TTL;

  private static final String KEY_PREFIX = "REFRESH_TOKEN:";
  private final RedisTemplate<String, String> redisTemplate;

  public void set(String key, String value) {
    redisTemplate.opsForValue().set(KEY_PREFIX + key, value, TTL);
  }

  public String get(String key) {
    return redisTemplate.opsForValue().get(KEY_PREFIX + key);
  }
}
