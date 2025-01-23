package com.heartsave.todaktodak_api.domain.auth.cache;

import static com.heartsave.todaktodak_api.common.constant.TodakConstant.AUTH.REFRESH_TOKEN_KEY_PREFIX;

import com.heartsave.todaktodak_api.common.security.jwt.config.JwtProperties;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCache {
  private final JwtProperties jwtProperties;

  private final RedisTemplate<String, String> redisTemplate;

  public void set(String key, String value) {
    redisTemplate
        .opsForValue()
        .set(
            REFRESH_TOKEN_KEY_PREFIX + key,
            value,
            jwtProperties.refreshExpireTime() / 1000,
            TimeUnit.SECONDS);
  }

  public String get(String key) {
    return redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX + key);
  }

  public void delete(String key) {
    redisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX + key);
  }
}
