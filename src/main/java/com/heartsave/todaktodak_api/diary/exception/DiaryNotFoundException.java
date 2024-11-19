package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import java.time.Instant;

public class DiaryNotFoundException extends DiaryException {

  public DiaryNotFoundException(DiaryErrorSpec errorSpec, Long memberId, Instant diaryDate) {
    super(errorSpec, memberId, diaryDate);
  }

  public DiaryNotFoundException(DiaryErrorSpec errorSpec, Long memberId, Long diaryId) {
    super(errorSpec, memberId, diaryId);
  }
}
