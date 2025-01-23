package com.heartsave.todaktodak_api.domain.ai.client.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.server.url")
public record AiServerProperties(String imageDomain, String textDomain, String bgmDomain) {}
