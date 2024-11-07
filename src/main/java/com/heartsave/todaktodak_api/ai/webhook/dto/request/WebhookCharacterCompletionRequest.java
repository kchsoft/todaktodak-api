package com.heartsave.todaktodak_api.ai.webhook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
@Schema(description = "AI 서버의 캐릭터 생성 완료 알림 요청 객체")
public record WebhookCharacterCompletionRequest(
    @NotNull @Schema(example = "1", description = "회원 아이디") Long memberId,
    @NotBlank @Schema(example = "{ \"hairs\" : \"long,black\" }", description = "캐릭터 특징")
        String characterInfo,
    @NotBlank @Schema(example = "romance", description = "캐릭터 화풍") String characterStyle,
    @NotNull @Positive @Schema(example = "13564", description = "캐릭터 이미지 시드") Integer seedNum,
    @NotBlank @Schema(example = "character/memberId", description = "캐릭터 이미지 경로")
        String characterUrl) {}
