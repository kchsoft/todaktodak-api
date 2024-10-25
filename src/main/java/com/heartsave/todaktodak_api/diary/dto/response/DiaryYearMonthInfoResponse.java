package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.dto.DiaryDateIndex;
import java.util.List;
import lombok.Getter;

@Getter
public class DiaryYearMonthInfoResponse {
  private List<DiaryDateIndex> diaryDateIndexes;
}
