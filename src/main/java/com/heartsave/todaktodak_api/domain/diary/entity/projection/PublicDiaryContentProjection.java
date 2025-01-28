package com.heartsave.todaktodak_api.domain.diary.entity.projection;

import com.heartsave.todaktodak_api.common.constant.TodakConstant.TIME_FORMAT;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@AllArgsConstructor
@Schema(description = "공개 일기 조회 프로젝션")
public class PublicDiaryContentProjection {

  @Schema(description = "공개 일기 ID", example = "1")
  private Long publicDiaryId;

  @Schema(description = "원본 일기 ID", example = "1")
  private Long diaryId;

  @Schema(description = "작성자 캐릭터 이미지 URL", example = "character/123")
  private String characterImageUrl;

  @Schema(description = "작성자 닉네임", example = "Todak")
  private String nickname;

  @Schema(description = "공개된 일기 내용", example = "오늘은 행복한 하루였습니다...")
  private String publicContent;

  @Schema(
      description = "DB에서 웹툰 이미지 폴더 URL 1개를 가져옵니다. 이후에 pre-signed URL 4개로 변환됩니다.",
      example = "[\"https://example.com/webtoon\"]")
  private String webtoonImageUrl;

  @Schema(description = "배경음악 URL", example = "https://example.com/bgm/123.mp3")
  private String bgmUrl;

  @Schema(description = "일기 작성 날짜", example = "2024-10-26", type = "string", format = "date")
  @DateTimeFormat(pattern = TIME_FORMAT.ISO_DATETIME_WITH_MILLISECONDS)
  private Instant date;
}
