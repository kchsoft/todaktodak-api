package com.heartsave.todaktodak_api.diary.entity.projection;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MySharedDiaryPreviewProjection {
  private final Long publicDiaryId;
  private String webtoonImageUrl;
  private final LocalDate createdDate;

  public void replaceWebtoonImageUrl(String webtoonImageUrl) {
    this.webtoonImageUrl = webtoonImageUrl;
  }
}
