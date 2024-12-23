package com.heartsave.todaktodak_api.ai.webhook.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.heartsave.todaktodak_api.common.constant.CoreConstant.TIME_FORMAT;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "AI 웹툰 저장 요청 데이터")
public record WebhookWebtoonCompletionRequest(
    @Schema(description = "회원 ID", example = "1", minimum = "1") @NotNull @Min(1L) Long memberId,
    @Schema(description = "일기 작성 날짜", example = "2024-11-06", format = "yyyy-MM-dd")
        @NotNull
        @DateTimeFormat(pattern = TIME_FORMAT.ISO_DATETIME_WITH_MILLISECONDS)
        @JsonProperty("date")
        Instant createdDate,
    @Schema(description = "웹툰 폴더 URL", example = "/webtoon/1/2024/11/06") @NotBlank
        String webtoonFolderUrl) {}
