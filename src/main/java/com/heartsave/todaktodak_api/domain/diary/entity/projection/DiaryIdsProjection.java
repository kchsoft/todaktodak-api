package com.heartsave.todaktodak_api.domain.diary.entity.projection;

import org.springframework.beans.factory.annotation.Value;

public interface DiaryIdsProjection {

  @Value("#{target.diaryId}")
  public Long getDiaryId();

  @Value("#{target.publicDiaryId}")
  public Long getPublicDiaryId();
}
