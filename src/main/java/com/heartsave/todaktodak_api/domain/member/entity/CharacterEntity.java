package com.heartsave.todaktodak_api.domain.member.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import org.springframework.data.redis.core.RedisHash;

@Builder
@RedisHash(value = "character", timeToLive = 172800) // 2일
public record CharacterEntity(
    @Id Long id,
    String characterInfo,
    String characterStyle,
    Integer characterSeed,
    String characterImageUrl) {}
