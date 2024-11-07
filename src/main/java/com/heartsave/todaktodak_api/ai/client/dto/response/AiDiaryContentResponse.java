package com.heartsave.todaktodak_api.ai.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiDiaryContentResponse {

  @JsonProperty("comment")
  private String aiComment;
}
