package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import java.time.LocalDate;
import java.util.List;

public record MySharedDiaryResponse(
    Long publicDiaryId,
    List<String> webtoonImageUrls,
    String publicContent,
    String bgmUrl,
    DiaryReactionCountProjection reactionCount,
    List<DiaryReactionType> myReaction,
    LocalDate diaryCreatedDate) {}
