package com.heartsave.todaktodak_api.diary.cache.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PublicDiaryContentSerializer
    implements CacheSerializer<List<PublicDiaryContentProjection>> {
  private final ObjectMapper objectMapper;

  public String serialize(List<PublicDiaryContentProjection> contents) {
    try {
      return objectMapper.writeValueAsString(contents);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

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
      return null;
    }
  }
}
