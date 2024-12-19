package com.heartsave.todaktodak_api.diary.cache;

import com.heartsave.todaktodak_api.diary.cache.entity.ContentReactionCountEntity;
import com.heartsave.todaktodak_api.diary.cache.serializer.PublicDiaryContentSerializer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class ContentReactionCountCache {
  private final String PUBLIC_DIARY_KEY = "public:diary:cache";
  private final int DEFAULT_SCORE = 0;
  private final RedisTemplate<String, String> redisTemplate;
  private final PublicDiaryContentSerializer serializer;

  public void save(List<ContentReactionCountEntity> contents) {
    if (!Boolean.TRUE.equals(redisTemplate.hasKey(PUBLIC_DIARY_KEY))) {
      redisTemplate.expire(PUBLIC_DIARY_KEY, Duration.ofMinutes(2));
    }

    contents.forEach(
        content ->
            redisTemplate
                .opsForZSet()
                .add(PUBLIC_DIARY_KEY, serializer.serialize(content), DEFAULT_SCORE));
  }

  public List<ContentReactionCountEntity> get(String orderPivot) {
    LinkedHashSet<String> resultSet =
        (LinkedHashSet<String>)
            redisTemplate
                .opsForZSet()
                .reverseRangeByLex(
                    PUBLIC_DIARY_KEY,
                    Range.leftUnbounded(Bound.exclusive(orderPivot)),
                    Limit.limit().offset(0).count(5));

    if (resultSet == null) return Collections.emptyList();
    List<ContentReactionCountEntity> contents = new ArrayList<>();
    resultSet.forEach(result -> contents.add(serializer.deserialize(result)));
    return contents;
  }
}
