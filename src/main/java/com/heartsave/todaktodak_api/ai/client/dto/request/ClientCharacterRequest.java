package com.heartsave.todaktodak_api.ai.client.dto.request;

import lombok.Builder;

@Builder
public record ClientCharacterRequest(Long memberId, String characterStyle) {}
