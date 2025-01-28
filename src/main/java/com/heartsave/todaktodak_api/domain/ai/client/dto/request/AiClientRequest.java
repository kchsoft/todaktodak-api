package com.heartsave.todaktodak_api.domain.ai.client.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.heartsave.todaktodak_api.domain.ai.client.util.AiClientDomainUrlUtil;
import lombok.Getter;

@Getter
public abstract class AiClientRequest {
  @JsonProperty("apiDomainUrl")
  private final String severDomainUrl;

  public AiClientRequest() {
    this.severDomainUrl = AiClientDomainUrlUtil.getServerDomainUrl();
  }
}
