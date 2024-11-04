package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryContentOnlyProjection;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MySharedDiaryResponse {
  private final Long publicDiaryId;
  private final List<String> webtoonImageUrls;
  private final String publicContent;
  private final String bgmUrl;
  private final DiaryReactionCountProjection reactionCount;
  private final List<DiaryReactionType> myReaction;
  private final LocalDate diaryCreatedDate;

  private MySharedDiaryResponse(
      MySharedDiaryContentOnlyProjection content,
      DiaryReactionCountProjection reactionCount,
      List<DiaryReactionType> myReaction) {
    this.publicDiaryId = content.getPublicDiaryId();
    this.webtoonImageUrls = content.getWebtoonImageUrls();
    this.publicContent = content.getPublicContent();
    this.bgmUrl = content.getBgmUrl();
    this.reactionCount = reactionCount;
    this.myReaction = myReaction;
    this.diaryCreatedDate = content.getPublicDiaryCreatedDate();
  }

  public static MySharedDiaryResponse of(
      MySharedDiaryContentOnlyProjection content,
      DiaryReactionCountProjection reactionCount,
      List<DiaryReactionType> myReaction) {
    return new MySharedDiaryResponse(content, reactionCount, myReaction);
  }
}
