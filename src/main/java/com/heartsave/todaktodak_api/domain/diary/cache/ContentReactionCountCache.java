package com.heartsave.todaktodak_api.domain.diary.cache;

import static com.heartsave.todaktodak_api.common.constant.TodakConstant.DIARY.PUBLIC_DIARY_PAGE_SIZE;

import com.heartsave.todaktodak_api.domain.diary.cache.entity.ContentReactionCountEntity;
import com.heartsave.todaktodak_api.domain.diary.cache.serializer.PublicDiaryContentSerializer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
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
      contents.forEach(
          content ->
              redisTemplate
                  .opsForZSet()
                  .add(PUBLIC_DIARY_KEY, serializer.serialize(content), DEFAULT_SCORE));
      redisTemplate.expire(PUBLIC_DIARY_KEY, Duration.ofMinutes(5));
    }

    contents.forEach(
        content ->
            redisTemplate
                .opsForZSet()
                .add(PUBLIC_DIARY_KEY, serializer.serialize(content), DEFAULT_SCORE));
  }

  public List<ContentReactionCountEntity> get(String orderPivot) {
    List<String> results =
        redisTemplate.execute(
            (RedisCallback<List<String>>)
                connection -> {
                  Object rawResponse =
                      connection.execute(
                          "ZRANGE",
                          PUBLIC_DIARY_KEY.getBytes(),
                          orderPivot.getBytes(),
                          "-".getBytes(),
                          "BYLEX".getBytes(),
                          "REV".getBytes(),
                          "LIMIT".getBytes(),
                          "0".getBytes(),
                          String.valueOf(PUBLIC_DIARY_PAGE_SIZE).getBytes());

                  List<String> stringList = new ArrayList<>();
                  if (rawResponse instanceof List<?> responseList) {
                    responseList.forEach(
                        item -> {
                          if (item instanceof byte[]) {
                            stringList.add(new String((byte[]) item));
                          }
                        });
                  }
                  return stringList;
                });
    List<ContentReactionCountEntity> contents = new ArrayList<>();
    results.forEach(result -> contents.add(serializer.deserialize(result)));
    return contents;
  }
}
