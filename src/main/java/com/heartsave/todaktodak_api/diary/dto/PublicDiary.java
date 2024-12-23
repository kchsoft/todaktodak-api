package com.heartsave.todaktodak_api.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.heartsave.todaktodak_api.common.constant.CoreConstant.TIME_FORMAT;
import com.heartsave.todaktodak_api.diary.cache.entity.ContentReactionCountEntity;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.domain.DiaryReactionCount;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.Getter;

@Schema(description = "공개 일기 상세 정보")
@Getter
public class PublicDiary {

  @Schema(description = "공개 일기 ID", example = "1")
  private final Long publicDiaryId;

  @Schema(description = "원본 일기 ID", example = "1")
  private final Long diaryId;

  @Schema(description = "작성자 캐릭터 이미지 URL", example = "\"character/123\"")
  private final String characterImageUrl;

  @Schema(description = "작성자 닉네임", example = "Todak")
  private final String nickname;

  @Schema(description = "공개된 일기 내용", example = "오늘은 행복한 하루였습니다...")
  private final String publicContent;

  @Schema(
      description = "일기 내용을 기반으로 생성된 웹툰 이미지 URL 목록",
      example =
          "[\"/webtoon/1/2024/11/06/1.webp\",\"/webtoon/1/2024/11/06/2.webp\",\"/webtoon/1/2024/11/06/3.webp\",\"/webtoon/1/2024/11/06/4.webp\"]")
  private final List<String> webtoonImageUrls;

  @Schema(description = "배경음악 URL", example = "/music-ai/1/2024/11/06/1.mp3")
  private final String bgmUrl;

  @Schema(description = "일기 작성 날짜", example = "2024-10-26", type = "string", format = "date")
  @JsonFormat(pattern = TIME_FORMAT.ISO_DATETIME_WITH_MILLISECONDS, timezone = "UTC")
  private final Instant createdDate;

  @Schema(description = "일기에 대한 반응 수 정보")
  private final DiaryReactionCount reactionCount;

  @Schema(description = "현재 사용자의 반응 목록")
  private final List<DiaryReactionType> myReaction;

  public static PublicDiary of(
      ContentReactionCountEntity contentReactionCount, List<DiaryReactionType> memberReaction) {
    return new PublicDiary(contentReactionCount, memberReaction);
  }

  private PublicDiary(
      ContentReactionCountEntity contentReactionCount, List<DiaryReactionType> memberReaction) {
    this.publicDiaryId = contentReactionCount.getPublicDiaryId();
    this.diaryId = contentReactionCount.getDiaryId();
    this.characterImageUrl = contentReactionCount.getCharacterImageUrl();
    this.nickname = contentReactionCount.getNickname();
    this.publicContent = contentReactionCount.getPublicContent();
    this.webtoonImageUrls = contentReactionCount.getWebtoonImageUrls();
    this.bgmUrl = contentReactionCount.getBgmUrl();
    this.createdDate = contentReactionCount.getDate();
    this.reactionCount = contentReactionCount.getReactionCount();
    this.myReaction = memberReaction;
  }
}
