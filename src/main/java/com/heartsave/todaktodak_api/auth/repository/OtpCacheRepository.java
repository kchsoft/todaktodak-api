package com.heartsave.todaktodak_api.auth.repository;

import static com.heartsave.todaktodak_api.common.config.constant.RedisConstant.OTP_KEY_PREFIX;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpCacheRepository {
  private final RedisTemplate<String, String> redisTemplate;
  private final Integer TTL = 3;

  public void set(String key, String value) {
    redisTemplate.opsForValue().set(OTP_KEY_PREFIX + key, value, TTL, TimeUnit.MINUTES);
  }

  public String get(String key) {
    return redisTemplate.opsForValue().get(OTP_KEY_PREFIX + key);
  }

  public void delete(String key) {
    redisTemplate.delete(OTP_KEY_PREFIX + key);
  }
}
