package com.heartsave.todaktodak_api.common.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

// 테스트를 위한 환경 설정 최소화를 위해 임베디드 레디스 구성
@Configuration
public class TempEmbeddedRedisConfig {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${spring.data.redis.port}")
  private int port;

  private RedisServer redisServer;

  @PostConstruct
  private void init() throws IOException {
    int redisPort = isRedisRunning() ? findAvailablePort() : port;
    logger.info("임베디드 레디스 초기화.. {} 포트 활성화", port);
    redisServer = new RedisServer(redisPort);
    redisServer.start();
  }

  @PreDestroy
  private void destroy() throws IOException {
    if (redisServer != null) {
      redisServer.stop();
    }
    logger.info("임베디드 레디스 종료.. {} 포트 비활성화", port);
  }

  private boolean isRedisRunning() throws IOException {
    return isRunning(findProcessByPort(port));
  }

  public int findAvailablePort() throws IOException {
    for (int port = 6379; port <= 65535; port++) {
      Process process = findProcessByPort(port);
      if (!isRunning(process)) {
        return port;
      }
    }
    throw new IllegalArgumentException("임베디드 레디스 포트 연결에 실패했습니다..");
  }

  private Process findProcessByPort(int port) throws IOException {
    String command = String.format("netstat -nat | grep LISTEN|grep %d", port);
    String[] shell = {"/bin/sh", "-c", command};
    return Runtime.getRuntime().exec(shell);
  }

  private boolean isRunning(Process process) {
    try (BufferedReader input =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      return input.readLine() != null;
    } catch (Exception e) {
      logger.error(e.getMessage());
      return false;
    }
  }
}
