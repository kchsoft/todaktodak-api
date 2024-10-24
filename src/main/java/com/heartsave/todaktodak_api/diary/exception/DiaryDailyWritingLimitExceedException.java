package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;

public class DiaryDailyWritingLimitExceedException extends DiaryException {

  public DiaryDailyWritingLimitExceedException(DiaryErrorSpec errorSpec, Long memberId) {
    super(errorSpec, memberId);
  }
}
