package com.heartsave.todaktodak_api.diary.cache.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.diary.domain.DiaryReactionCount;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DiaryReactionSerializer implements CacheSerializer<DiaryReactionCount> {
  private final ObjectMapper objectMapper;

  public String serialize(DiaryReactionCount count) {
    try {
      return objectMapper.writeValueAsString(count);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public DiaryReactionCount deserialize(String value) {
    try {
      return objectMapper.readValue(value, DiaryReactionCount.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
