package com.heartsave.todaktodak_api.diary.dto;

import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public record PublicDiaryViewDetail(
    Long publicDiaryId,
    String characterImageUrl,
    String nickname,
    String publicContent,
    String webtoonImageUrl,
    String bgmUrl,
    LocalDate date,
    DiaryReactionCountProjection reactionCount,
    List<DiaryReactionType> myReaction) {}
