package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryContentOnlyProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "나의 공개된 일기 상세 조회 응답")
@AllArgsConstructor
@Getter
public class MySharedDiaryResponse {

  @Schema(description = "공개된 일기 ID", example = "1")
  private final Long publicDiaryId;

  @Schema(
      description = "웹툰 이미지 URL 목록",
      example = "[https://example.com/webtoon/123.jpg, https://example.com/webtoon/124.jpg]")
  private final List<String> webtoonImageUrls;

  @Schema(description = "공개된 일기 내용", example = "오늘은 정말 좋은 하루였다...")
  private final String publicContent;

  @Schema(description = "배경음악 URL", example = "https://example.com/music/123.mp3")
  private final String bgmUrl;

  @Schema(description = "일기에 대한 리액션 수 정보")
  private final DiaryReactionCountProjection reactionCount;

  @Schema(description = "내가 한 리액션 목록", example = "[like, empathize]")
  private final List<DiaryReactionType> myReaction;

  @Schema(description = "일기 작성 날짜", example = "2024-01-01", type = "string", format = "date")
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
    this.diaryCreatedDate = content.getDiaryCreatedDate();
  }

  public static MySharedDiaryResponse of(
      MySharedDiaryContentOnlyProjection content,
      DiaryReactionCountProjection reactionCount,
      List<DiaryReactionType> myReaction) {
    return new MySharedDiaryResponse(content, reactionCount, myReaction);
  }
}
