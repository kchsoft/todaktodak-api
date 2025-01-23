package com.heartsave.todaktodak_api.domain.ai.client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiClientCommentResponse {

  @JsonProperty("comment")
  private String aiComment;
}
