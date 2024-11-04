package com.heartsave.todaktodak_api.diary.entity.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "공개된 일기 미리보기 Projection")
@Getter
@AllArgsConstructor
public class MySharedDiaryPreviewProjection {

  @Schema(description = "공개된 일기 ID", example = "1")
  private final Long publicDiaryId;

  @Schema(description = "웹툰 이미지 URL", example = "https://example.com/webtoon/123.jpg")
  private String webtoonImageUrl;

  @Schema(description = "일기 작성 날짜", example = "2024-01-01", type = "string", format = "date")
  private final LocalDate createdDate;

  public void replaceWebtoonImageUrl(String webtoonImageUrl) {
    this.webtoonImageUrl = webtoonImageUrl;
  }
}
