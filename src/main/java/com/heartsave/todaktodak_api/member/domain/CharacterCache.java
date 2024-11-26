package com.heartsave.todaktodak_api.member.domain;

import jakarta.persistence.Id;
import lombok.Builder;
import org.springframework.data.redis.core.RedisHash;

@Builder
@RedisHash(value = "character", timeToLive = 172800) // 2일
public record CharacterCache(
    @Id Long id,
    String characterInfo,
    String characterStyle,
    Integer characterSeed,
    String characterImageUrl) {}
