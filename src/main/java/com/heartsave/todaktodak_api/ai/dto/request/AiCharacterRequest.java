package com.heartsave.todaktodak_api.ai.dto.request;

import lombok.Builder;

@Builder
public record AiCharacterRequest(Long memberId, String characterStyle) {}
