package com.heartsave.todaktodak_api.diary.dto;

import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "공개 일기 상세 정보")
@Builder
@Getter
public class PublicDiaryViewDetail {

  @Schema(description = "공개 일기 ID", example = "1")
  private final Long publicDiaryId;

  @Schema(description = "원본 일기 ID", example = "1")
  private final Long diaryId;

  @Schema(description = "작성자 캐릭터 이미지 URL", example = "https://example.com/character/123.jpg")
  private final String characterImageUrl;

  @Schema(description = "작성자 닉네임", example = "Todak")
  private final String nickname;

  @Schema(description = "공개된 일기 내용", example = "오늘은 행복한 하루였습니다...")
  private final String publicContent;

  private final List<String> webtoonUrls;

  @Schema(description = "배경음악 URL", example = "https://example.com/bgm/123.mp3")
  private final String bgmUrl;

  @Schema(description = "일기 작성 날짜", example = "2024-10-26", type = "string", format = "date")
  private final LocalDate date;

  @Schema(description = "일기에 대한 반응 수 정보")
  private final DiaryReactionCountProjection reactionCount;

  @Schema(description = "현재 사용자의 반응 목록")
  private final List<DiaryReactionType> myReaction;

  public PublicDiaryViewDetail(
      PublicDiaryView view,
      DiaryReactionCountProjection reactionCount,
      List<DiaryReactionType> memberReaction) {
    this.publicDiaryId = view.getPublicDiaryId();
    this.diaryId = view.getDiaryId();
    this.characterImageUrl = view.getCharacterImageUrl();
    this.nickname = view.getNickname();
    this.publicContent = view.getPublicContent();
    this.webtoonUrls = view.getWebtoonUrls();
    this.bgmUrl = view.getBgmUrl();
    this.date = view.getDate();
    this.reactionCount = reactionCount;
    this.myReaction = memberReaction;
  }
}
