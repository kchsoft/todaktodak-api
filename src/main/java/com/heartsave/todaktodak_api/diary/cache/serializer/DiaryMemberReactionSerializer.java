package com.heartsave.todaktodak_api.diary.cache.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DiaryMemberReactionSerializer implements CacheSerializer<List<DiaryReactionType>> {
  private final ObjectMapper objectMapper;

  @Override
  public String serialize(List<DiaryReactionType> reactionTypes) {
    try {
      return objectMapper.writeValueAsString(reactionTypes);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public List<DiaryReactionType> deserialize(String reactionTypes) {
    try {
      return objectMapper.readValue(
          reactionTypes,
          objectMapper
              .getTypeFactory()
              .constructCollectionType(List.class, DiaryReactionType.class));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) { // catch null
      return null;
    }
  }
}
