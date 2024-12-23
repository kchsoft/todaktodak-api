package com.heartsave.todaktodak_api.diary.cache.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.diary.cache.entity.ContentReactionCountEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PublicDiaryContentSerializer implements CacheSerializer<ContentReactionCountEntity> {
  private final ObjectMapper objectMapper;

  public String serialize(ContentReactionCountEntity contents) {
    try {
      return objectMapper.writeValueAsString(contents);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public ContentReactionCountEntity deserialize(String jsonValue) {
    try {
      return objectMapper.readValue(jsonValue, ContentReactionCountEntity.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    } catch (IllegalArgumentException e) { // catch null
      throw new IllegalArgumentException("contentReactionCount deserialize fail, value is null");
    }
  }
}
