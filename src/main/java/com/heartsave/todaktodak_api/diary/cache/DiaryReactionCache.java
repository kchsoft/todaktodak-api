package com.heartsave.todaktodak_api.diary.cache;

import com.heartsave.todaktodak_api.diary.cache.serializer.DiaryMemberReactionSerializer;
import com.heartsave.todaktodak_api.diary.cache.serializer.DiaryReactionCountSerializer;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.domain.DiaryReactionCount;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class DiaryReactionCache {
  private final String REACTION_COUNT_HASH_KEY = "reaction:count:%d";
  private final String REACTION_MEMBER_HASH_KEY = "reaction:member:%d:public_diary:%d";
  private final RedisTemplate<String, String> redisTemplate;
  private final DiaryReactionCountSerializer reactionCountSerializer;
  private final DiaryMemberReactionSerializer memberReactionSerializer;

  public void saveCount(String key, Long publicDiaryId, DiaryReactionCount count) {
    String serialize = reactionCountSerializer.serialize(count);

    if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
      redisTemplate.opsForHash().put(key, getCountHashKey(publicDiaryId), serialize);
      redisTemplate.expire(key, Duration.ofMinutes(2));
      return;
    }
    redisTemplate.opsForHash().put(key, getCountHashKey(publicDiaryId), serialize);
  }

  public Optional<DiaryReactionCount> getCount(String key, Long publicDiaryId) {
    String str = (String) redisTemplate.opsForHash().get(key, getCountHashKey(publicDiaryId));
    return Optional.ofNullable(reactionCountSerializer.deserialize(str));
  }

  public void saveMemberReactions(
      String key, Long memberId, Long publicDiaryId, List<DiaryReactionType> memberReactions) {
    String serialize = memberReactionSerializer.serialize(memberReactions);
    if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
      redisTemplate
          .opsForHash()
          .put(key, getMemberReactionHashKey(memberId, publicDiaryId), serialize);
      redisTemplate.expire(key, Duration.ofMinutes(1));
      return;
    }
    redisTemplate
        .opsForHash()
        .put(key, getMemberReactionHashKey(memberId, publicDiaryId), serialize);
  }

  public List<DiaryReactionType> getMemberReactions(String key, Long memberId, Long publicDiaryId) {
    String s =
        (String)
            redisTemplate.opsForHash().get(key, getMemberReactionHashKey(memberId, publicDiaryId));
    return memberReactionSerializer.deserialize(s);
  }

  private String getCountHashKey(Long publicDiaryId) {
    return String.format(REACTION_COUNT_HASH_KEY, publicDiaryId);
  }

  private String getMemberReactionHashKey(Long memberId, Long publicDiaryId) {
    return String.format(REACTION_MEMBER_HASH_KEY, memberId, publicDiaryId);
  }
}
