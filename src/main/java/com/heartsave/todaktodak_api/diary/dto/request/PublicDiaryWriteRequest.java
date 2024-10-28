package com.heartsave.todaktodak_api.diary.dto.request;

import static com.heartsave.todaktodak_api.diary.constant.DiaryContentConstraintConstant.PUBLIC_DIARY_CONTENT_MAX_SIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "공개 일기 작성 요청 데이터")
public record PublicDiaryWriteRequest(
    @Schema(description = "일기 ID", example = "1", minimum = "1")
        @Min(value = 1, message = "diaryId의 값은 최소 1 이상이어야 합니다.")
        Long diaryId,
    @Schema(
            description = "공개 일기 내용",
            example = "오늘의 일기를 공유합니다...",
            maxLength = PUBLIC_DIARY_CONTENT_MAX_SIZE)
        @Size(max = PUBLIC_DIARY_CONTENT_MAX_SIZE, message = "공개 일기 내용이 너무 깁니다.")
        String publicContent) {}
