package com.heartsave.todaktodak_api.common.security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secretKey, Long accessExpireTime, Long refreshExpireTime) {}
