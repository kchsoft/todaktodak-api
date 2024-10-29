package com.heartsave.todaktodak_api.diary.dto.request;

import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PublicDiaryReactionRequest(
    @Min(1L) Long diaryId, @NotBlank DiaryReactionType reactionType) {}
