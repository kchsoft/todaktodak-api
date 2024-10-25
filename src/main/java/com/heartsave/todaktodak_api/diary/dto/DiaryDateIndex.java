package com.heartsave.todaktodak_api.diary.dto;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class DiaryDateIndex {
  private Long diaryId;
  private LocalDateTime date;
}
