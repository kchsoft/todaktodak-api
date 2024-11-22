package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.ErrorFieldBuilder;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;

public class PublicDiaryExistException extends PublicDiaryException {

  public PublicDiaryExistException(ErrorSpec errorSpec, Long memberId, Long publicDiaryId) {
    super(
        errorSpec,
        ErrorFieldBuilder.builder()
            .add("memberId", memberId)
            .add("publicDiaryId", publicDiaryId)
            .build());
  }
}
