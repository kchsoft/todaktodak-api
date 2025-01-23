package com.heartsave.todaktodak_api.domain.diary.exception;

import com.heartsave.todaktodak_api.common.exception.ErrorFieldBuilder;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;
import java.time.Instant;

public class PublicDiaryNotFoundException extends PublicDiaryException {

  public PublicDiaryNotFoundException(ErrorSpec errorSpec, Long publicDiaryId) {
    super(errorSpec, ErrorFieldBuilder.builder().add("publicDiaryId", publicDiaryId).build());
  }

  public PublicDiaryNotFoundException(ErrorSpec errorSpec, Instant publicDiaryCreatedDate) {
    super(
        errorSpec,
        ErrorFieldBuilder.builder().add("publicDiaryCreatedDate", publicDiaryCreatedDate).build());
  }
}
