package com.heartsave.todaktodak_api.ai.client.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.heartsave.todaktodak_api.ai.client.util.ClientIpUtil;
import lombok.Getter;

@Getter
public abstract class ClientIPInfo {
  @JsonProperty("apiIp")
  private final String severIp;

  public ClientIPInfo() {
    this.severIp = ClientIpUtil.getServerIp();
  }
}
