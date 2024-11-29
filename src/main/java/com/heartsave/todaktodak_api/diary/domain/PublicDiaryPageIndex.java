package com.heartsave.todaktodak_api.diary.domain;

import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryPageRequest;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryPageIndexProjection;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PublicDiaryPageIndex {
  private final Long publicDiaryId;
  private final Instant createdTime;

  public static PublicDiaryPageIndex fromLatest(PublicDiaryPageIndexProjection projection) {
    return new PublicDiaryPageIndex(
        projection.getPublicDiaryId() + 1L, projection.getCreatedTime());
  }

  public static PublicDiaryPageIndex from(PublicDiaryPageRequest request) {
    return new PublicDiaryPageIndex(request.publicDiaryId(), request.createdTime());
  }
}
