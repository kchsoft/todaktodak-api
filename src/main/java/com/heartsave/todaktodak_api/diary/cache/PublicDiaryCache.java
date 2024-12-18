package com.heartsave.todaktodak_api.diary.cache;

import com.heartsave.todaktodak_api.diary.cache.serializer.PublicDiaryContentSerializer;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class PublicDiaryCache {

  private final RedisTemplate<String, String> redisTemplate;
  private final PublicDiaryContentSerializer serializer;

  public void save(
      String key, DiaryPageIndex pageIndex, List<PublicDiaryContentProjection> contents) {
    if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
      redisTemplate.expire(key, Duration.ofMinutes(1));
    }
    redisTemplate.opsForHash().put(key, getHashKey(pageIndex), serializer.serialize(contents));
  }

  public Optional<List<PublicDiaryContentProjection>> get(String key, DiaryPageIndex pageIndex) {
    return Optional.ofNullable(
        serializer.deserialize(
            (String) redisTemplate.opsForHash().get(key, getHashKey(pageIndex))));
  }

  private String getHashKey(DiaryPageIndex pageIndex) {
    return pageIndex.getCreatedTime().toEpochMilli() + ":" + pageIndex.getPublicDiaryId();
  }
}
