package com.heartsave.todaktodak_api.diary.entity.projection;

import java.util.List;

public class MySharedDiaryContentOnlyProjection {

  private final Long id;
  private final String publicContent;
  private List<String> webtoonImageUrls;
  private String bgmUrl;

  public MySharedDiaryContentOnlyProjection(
      Long id, String publicContent, String webtoonImageUrl, String bgmUrl) {
    this.id = id;
    this.publicContent = publicContent;
    this.webtoonImageUrls = List.of(webtoonImageUrl);
    this.bgmUrl = bgmUrl;
  }

  public void replaceWebtoonImageUrls(List<String> urls) {
    this.webtoonImageUrls = urls;
  }

  public void replaceBgmUrl(String url) {
    this.bgmUrl = url;
  }
}
