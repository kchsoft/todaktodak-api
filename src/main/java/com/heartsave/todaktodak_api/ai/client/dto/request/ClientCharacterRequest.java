package com.heartsave.todaktodak_api.ai.client.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientCharacterRequest extends ClientIPInfo {
  private final Long memberId;
  private final String characterStyle;

  public ClientCharacterRequest(Long memberId, String characterStyle) {
    this.memberId = memberId;
    this.characterStyle = characterStyle;
  }
}
