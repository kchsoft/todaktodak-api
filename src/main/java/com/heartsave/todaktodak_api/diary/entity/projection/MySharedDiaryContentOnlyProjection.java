package com.heartsave.todaktodak_api.diary.entity.projection;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class MySharedDiaryContentOnlyProjection {

  private final Long publicDiaryId;
  private final Long diaryId;
  private final String publicContent;
  private List<String> webtoonImageUrls;
  private String bgmUrl;
  private LocalDate publicDiaryCreatedDate;

  public MySharedDiaryContentOnlyProjection(
      Long publicDiaryId,
      Long diaryId,
      String publicContent,
      String webtoonImageUrl,
      String bgmUrl,
      LocalDate publicDiaryCreatedDate) {
    this.publicDiaryId = publicDiaryId;
    this.diaryId = diaryId;
    this.publicContent = publicContent;
    this.webtoonImageUrls = List.of(webtoonImageUrl);
    this.bgmUrl = bgmUrl;
    this.publicDiaryCreatedDate = publicDiaryCreatedDate;
  }

  public void replaceWebtoonImageUrls(List<String> urls) {
    this.webtoonImageUrls = urls;
  }

  public void replaceBgmUrl(String url) {
    this.bgmUrl = url;
  }
}
