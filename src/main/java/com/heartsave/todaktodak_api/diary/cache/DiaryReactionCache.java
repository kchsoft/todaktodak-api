package com.heartsave.todaktodak_api.diary.cache;

import com.heartsave.todaktodak_api.diary.cache.serializer.DiaryReactionSerializer;
import com.heartsave.todaktodak_api.diary.domain.DiaryReactionCount;
import java.time.Duration;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class DiaryReactionCache {
  private final String REACTION_COUNT_HASH_KEY = "reaction:count";
  private final String REACTION_MY_HASH_KEY = "reaction:my";
  private final RedisTemplate<String, String> redisTemplate;
  private final DiaryReactionSerializer serializer;

  public void saveCount(String key, Long publicDiaryId, DiaryReactionCount count) {
    if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
      redisTemplate
          .opsForHash()
          .put(key, getCountHashKey(publicDiaryId), serializer.serialize(count));
      redisTemplate.expire(key, Duration.ofMinutes(1));
      return;
    }
    redisTemplate
        .opsForHash()
        .put(key, getCountHashKey(publicDiaryId), serializer.serialize(count));
  }

  public Optional<DiaryReactionCount> getCount(String key, Long publicDiaryId) {
    return Optional.ofNullable(
        serializer.deserialize(
            (String) redisTemplate.opsForHash().get(key, getCountHashKey(publicDiaryId))));
  }

  private String getCountHashKey(Long publicDiaryId) {
    return REACTION_COUNT_HASH_KEY + ":" + publicDiaryId;
  }

  private String getMyHashKey(Long publicDiaryId) {
    return REACTION_MY_HASH_KEY + ":" + publicDiaryId;
  }
}
