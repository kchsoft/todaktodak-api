package com.heartsave.todaktodak_api.domain.ai.client.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiClientCharacterRequest extends AiClientRequest {
  private final Long memberId;
  private final String characterStyle;

  public AiClientCharacterRequest(Long memberId, String characterStyle) {
    this.memberId = memberId;
    this.characterStyle = characterStyle;
  }
}
