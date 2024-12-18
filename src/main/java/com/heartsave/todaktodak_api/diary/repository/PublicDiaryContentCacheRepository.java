package com.heartsave.todaktodak_api.diary.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PublicDiaryContentCacheRepository
    implements CacheRepository<List<PublicDiaryContentProjection>> {
  private final String PUBLIC_DIARY_CACHE_KEY = "public:diary:cache";
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  public PublicDiaryContentCacheRepository(
      RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public void save(DiaryPageIndex pageIndex, List<PublicDiaryContentProjection> contents) {
    if (Boolean.FALSE.equals(redisTemplate.hasKey(PUBLIC_DIARY_CACHE_KEY))) {
      redisTemplate.expire(PUBLIC_DIARY_CACHE_KEY, Duration.ofMinutes(1));
    }
    redisTemplate
        .opsForHash()
        .put(PUBLIC_DIARY_CACHE_KEY, getHasKey(pageIndex), serialize(contents));
  }

  public List<PublicDiaryContentProjection> get(DiaryPageIndex pageIndex) {
    List<PublicDiaryContentProjection> value =
        deserialize(
            (String) redisTemplate.opsForHash().get(PUBLIC_DIARY_CACHE_KEY, getHasKey(pageIndex)));
    return value;
  }

  private String getHasKey(DiaryPageIndex pageIndex) {
    return pageIndex.getCreatedTime().toEpochMilli() + ":" + pageIndex.getPublicDiaryId();
  }

  @Override
  public String serialize(List<PublicDiaryContentProjection> contents) {
    try {
      return objectMapper.writeValueAsString(contents);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public List<PublicDiaryContentProjection> deserialize(String jsonValue) {
    try {
      return objectMapper.readValue(
          jsonValue,
          objectMapper
              .getTypeFactory()
              .constructCollectionType(List.class, PublicDiaryContentProjection.class));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) { // catch null
      return Collections.emptyList();
    }
  }
}
