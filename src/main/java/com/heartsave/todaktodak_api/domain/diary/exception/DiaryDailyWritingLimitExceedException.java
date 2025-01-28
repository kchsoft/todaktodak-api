package com.heartsave.todaktodak_api.domain.diary.exception;

import com.heartsave.todaktodak_api.common.exception.errorspec.diary.DiaryErrorSpec;

public class DiaryDailyWritingLimitExceedException extends DiaryException {

  public DiaryDailyWritingLimitExceedException(DiaryErrorSpec errorSpec, Long memberId) {
    super(errorSpec, memberId);
  }
}
