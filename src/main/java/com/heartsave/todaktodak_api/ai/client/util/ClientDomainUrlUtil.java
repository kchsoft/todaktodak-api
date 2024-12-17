package com.heartsave.todaktodak_api.ai.client.util;

import jakarta.annotation.PostConstruct;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientDomainUrlUtil {

  @Value("${server.port}")
  private Integer port;

  private static String SERVER_DOMAIN_URL;

  @PostConstruct
  public void init() {
    SERVER_DOMAIN_URL = getServerIp() + ":" + port;
  }

  public static String getServerDomainUrl() {
    return SERVER_DOMAIN_URL;
  }

  private String getServerIp() {
    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface current = networkInterfaces.nextElement();
        if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
          continue;
        }
        Enumeration<InetAddress> inetAddresses = current.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
          InetAddress inetAddress = inetAddresses.nextElement();
          // IPv4 주소만 필터링
          if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
            return inetAddress.getHostAddress();
          }
        }
      }
      return "IP 주소를 가져올 수 없습니다.";
    } catch (SocketException e) {
      throw new RuntimeException("IP 주소를 가져오는 도중, 에러가 발생했습니다.");
    }
  }
}
