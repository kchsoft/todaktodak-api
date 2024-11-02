package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.ErrorFieldBuilder;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;

public class PublicDiaryNotFoundException extends PublicDiaryException {

  public PublicDiaryNotFoundException(ErrorSpec errorSpec, Long publicDiaryId) {
    super(errorSpec, ErrorFieldBuilder.builder().add("publicDiaryId", publicDiaryId).build());
  }
}
