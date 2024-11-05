package com.heartsave.todaktodak_api.ai.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

@Configuration
public class AiServerWebClientConfig {

  @Value("${ai.server.url.domain}")
  private String AI_SERVER_URL_DOMAIN;

  @Bean
  public WebClient aiWebClient(Builder webClientBuilder) {
    return webClientBuilder
        .baseUrl(AI_SERVER_URL_DOMAIN)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
