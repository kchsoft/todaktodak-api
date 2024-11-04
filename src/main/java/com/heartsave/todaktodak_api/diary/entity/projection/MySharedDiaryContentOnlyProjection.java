package com.heartsave.todaktodak_api.diary.entity.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Schema(description = "공개된 일기 상세 내용 Projection")
@Getter
public class MySharedDiaryContentOnlyProjection {

  @Schema(description = "공개된 일기 ID", example = "1")
  private final Long publicDiaryId;

  @Schema(description = "원본 일기 ID", example = "1")
  private final Long diaryId;

  @Schema(description = "공개된 일기 내용", example = "오늘은 정말 좋은 하루였다...")
  private final String publicContent;

  @Schema(
      description = "웹툰 이미지 URL 목록",
      example = "[https://example.com/webtoon/123.jpg, https://example.com/webtoon/124.jpg]")
  private List<String> webtoonImageUrls;

  @Schema(description = "배경음악 URL", example = "https://example.com/music/123.mp3")
  private String bgmUrl;

  @Schema(description = "공개된 일기 작성 날짜", example = "2024-01-01", type = "string", format = "date")
  private final LocalDate publicDiaryCreatedDate;

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
