package com.heartsave.todaktodak_api.common.security.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secretKey, Long accessExpireTime, Long refreshExpireTime) {}
