package com.heartsave.todaktodak_api.ai.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public record AiCharacterRequest(Long memberId, String characterStyle) {}
