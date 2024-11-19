package com.heartsave.todaktodak_api.diary.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.heartsave.todaktodak_api.common.constant.CoreConstant.TIME_FORMAT;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "일기 상세 조회 응답")
@Getter
@Builder
public class DiaryResponse {

  @Schema(description = "일기 ID", example = "1")
  private Long diaryId;

  @Schema(description = "일기에 기록된 감정", example = "HAPPY")
  private DiaryEmotion emotion;

  @Schema(description = "일기 내용", example = "오늘은 정말 좋은 하루였다...")
  private String content;

  @Schema(
      description = "웹툰 이미지 URL",
      example =
          "[\"/webtoon/1/2024/11/06/1.webp\",\"/webtoon/1/2024/11/06/2.webp\",\"/webtoon/1/2024/11/06/3.webp\",\"/webtoon/1/2024/11/06/4.webp\"]")
  private List<String> webtoonImageUrls;

  @Schema(description = "배경음악 URL", example = "/music-ai/1/2024/11/06/1.mp3")
  private String bgmUrl;

  @Schema(description = "AI가 작성한 코멘트", example = "오늘 하루도 수고 많으셨어요!")
  private String aiComment;

  @Schema(description = "일기 작성 날짜", example = "2024-10-26", type = "string", format = "date")
  @JsonFormat(pattern = TIME_FORMAT.ISO_DATETIME_WITH_MILLISECONDS, timezone = "UTC")
  private Instant dateTime;
}
