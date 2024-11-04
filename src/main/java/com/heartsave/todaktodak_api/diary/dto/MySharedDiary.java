package com.heartsave.todaktodak_api.diary.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MySharedDiary {
  private final Long publicDiaryId;
  private List<String> webtoonImageUrls;
  private String publicContent;
  private String bgmUrl;
  private final LocalDate diaryCreatedDate;
}
