package com.heartsave.todaktodak_api.diary.dto.response;

import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIndexProjection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryIndexResponse {
  private final List<DiaryIndexProjection> diaryIndexes;
}
