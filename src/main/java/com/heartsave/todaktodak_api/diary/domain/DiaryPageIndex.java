package com.heartsave.todaktodak_api.diary.domain;

import com.heartsave.todaktodak_api.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryPageIndexProjection;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiaryPageIndex {
  private final Long publicDiaryId;
  private final Instant createdTime;

  public static DiaryPageIndex fromLatest(DiaryPageIndexProjection projection) {
    return new DiaryPageIndex(projection.getPublicDiaryId() + 1L, projection.getCreatedTime());
  }

  public static DiaryPageIndex from(DiaryPageRequest request) {
    return new DiaryPageIndex(request.publicDiaryId(), request.createdTime());
  }

  public Long getMilsTimeStamp() {
    return createdTime.toEpochMilli();
  }
}
