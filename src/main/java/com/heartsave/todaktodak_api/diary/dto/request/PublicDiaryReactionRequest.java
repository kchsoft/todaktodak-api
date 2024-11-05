package com.heartsave.todaktodak_api.diary.dto.request;

import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "공개 일기 반응 요청 데이터")
public record PublicDiaryReactionRequest(
    @Schema(description = "일기 ID", example = "1", minimum = "1") @Min(1L) Long diaryId,
    @Schema(description = "반응 타입", example = "like", required = true) @NotNull
        DiaryReactionType reactionType) {}
