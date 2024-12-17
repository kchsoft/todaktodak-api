package com.heartsave.todaktodak_api.ai.client.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.heartsave.todaktodak_api.ai.client.util.ClientDomainUrlUtil;
import lombok.Getter;

@Getter
public abstract class ClientDomainInfo {
  @JsonProperty("apiDomainUrl")
  private final String severDomainUrl;

  public ClientDomainInfo() {
    this.severDomainUrl = ClientDomainUrlUtil.getServerDomainUrl();
  }
}
