package com.heartsave.todaktodak_api.ai.webhook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "AI 서버의 캐릭터 생성 완료 알림 요청 객체")
public record AiWebhookCharacterRequest(
    @Schema(example = "1", description = "회원 아이디") Long memberId,
    @Schema(example = "{ \"hairs\" : \"long,black\" }", description = "캐릭터 특징")
        String characterInfo,
    @Schema(example = "romance", description = "캐릭터 화풍") String characterStyle,
    @Schema(example = "13564", description = "캐릭터 이미지 시드") Integer seedNum,
    @Schema(example = "character/memberId", description = "캐릭터 이미지 경로") String characterUrl) {}
