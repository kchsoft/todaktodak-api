package com.heartsave.todaktodak_api.diary.entity.projection;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicDiaryPageIndexProjection {
  private final Long publicDiaryId;
  private final Instant createdTime;
}
