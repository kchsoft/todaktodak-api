package com.heartsave.todaktodak_api.diary.entity.projection;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;

public interface DiaryPageIndexProjection {
  @Value("#{target.id}")
  Long getPublicDiaryId();

  Instant getCreatedTime();
}
