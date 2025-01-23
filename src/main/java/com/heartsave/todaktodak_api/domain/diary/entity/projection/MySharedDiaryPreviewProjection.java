package com.heartsave.todaktodak_api.domain.diary.entity.projection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.heartsave.todaktodak_api.common.constant.TodakConstant.TIME_FORMAT;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
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
  @JsonFormat(pattern = TIME_FORMAT.ISO_DATETIME_WITH_MILLISECONDS, timezone = "UTC")
  private final Instant createdDate;

  public void replaceWebtoonImageUrl(String webtoonImageUrl) {
    this.webtoonImageUrl = webtoonImageUrl;
  }
}
