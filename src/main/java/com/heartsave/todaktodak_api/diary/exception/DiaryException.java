package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorFieldBuilder;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import lombok.Getter;

@Getter
public abstract class DiaryException extends BaseException {

  protected DiaryException(DiaryErrorSpec errorSpec, Long memberId, Long diaryId) {
    super(
        errorSpec,
        ErrorFieldBuilder.builder().add("memberId", memberId).add("diaryId", diaryId).build());
  }

  protected DiaryException(DiaryErrorSpec errorSpec, Long memberId) {
    super(errorSpec, ErrorFieldBuilder.builder().add("memberId", memberId).build());
  }
}
