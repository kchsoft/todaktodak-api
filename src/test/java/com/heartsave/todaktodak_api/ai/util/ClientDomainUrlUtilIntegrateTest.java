package com.heartsave.todaktodak_api.ai.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.ai.client.util.ClientDomainUrlUtil;
import com.heartsave.todaktodak_api.config.BaseIntegrateTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClientDomainUrlUtilIntegrateTest extends BaseIntegrateTest {

  @Test
  @DisplayName("Spring API 서버의 IP를 추출합니다.")
  void getSpringApiServerIp() {
    String serverDomainUrl = ClientDomainUrlUtil.getServerDomainUrl();
    String[] serverIpAndPort = serverDomainUrl.split(":");
    String[] serverIps = serverIpAndPort[0].split("\\.");
    assertThat(serverIps).as("server IP는 길이 4 배열이어야 합니다.").hasSize(4);
    for (String part : serverIps) {
      int num = Integer.parseInt(part);
      assertThat(num).as("server IP의 각 값은 0이상, 255이하 이어야 합니다.").isBetween(0, 255);
    }
    System.out.println("serverDomainUrl = " + serverDomainUrl);
  }
}
