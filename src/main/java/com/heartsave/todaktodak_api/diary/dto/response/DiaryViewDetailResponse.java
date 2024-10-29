package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "일기 상세 조회 응답")
@Getter
@Builder
public class DiaryViewDetailResponse {

  @Schema(description = "일기 ID", example = "1")
  private Long diaryId;

  @Schema(description = "일기에 기록된 감정", example = "HAPPY")
  private DiaryEmotion emotion;

  @Schema(description = "일기 내용", example = "오늘은 정말 좋은 하루였다...")
  private String content;

  @Schema(description = "웹툰 이미지 URL", example = "https://example.com/webtoon/123.jpg")
  private String webtoonImageUrl;

  @Schema(description = "배경음악 URL", example = "https://example.com/music/123.mp3")
  private String bgmUrl;

  @Schema(description = "AI가 작성한 코멘트", example = "오늘 하루도 수고 많으셨어요!")
  private String aiComment;

  @Schema(description = "일기에 대한 반응 수 정보")
  private DiaryReactionCountProjection reactionCount;

  @Schema(description = "일기 작성 날짜", example = "2024-10-26", type = "string", format = "date")
  private LocalDate date;
}
