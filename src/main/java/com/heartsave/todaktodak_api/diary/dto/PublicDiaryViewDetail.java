package com.heartsave.todaktodak_api.diary.dto;

import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryViewProjection;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PublicDiaryViewDetail {
  private final Long publicDiaryId;
  private final Long diaryId;
  private final String characterImageUrl;
  private final String nickname;
  private final String publicContent;
  private final String webtoonUrl;
  private final String bgmUrl;
  private final LocalDate date;
  private final DiaryReactionCountProjection reactionCount;
  private final List<DiaryReactionType> myReaction;

  public static PublicDiaryViewDetail of(
      PublicDiaryViewProjection view,
      DiaryReactionCountProjection reactionCount,
      List<DiaryReactionType> memberReaction) {
    return PublicDiaryViewDetail.builder()
        .publicDiaryId(view.getPublicDiaryId())
        .diaryId(view.getDiaryId())
        .characterImageUrl(view.getCharacterImageUrl())
        .nickname(view.getNickname())
        .publicContent(view.getPublicContent())
        .webtoonUrl(view.getWebtoonImageUrl())
        .bgmUrl(view.getBgmUrl())
        .date(view.getDate())
        .reactionCount(reactionCount)
        .myReaction(memberReaction)
        .build();
  }
}
