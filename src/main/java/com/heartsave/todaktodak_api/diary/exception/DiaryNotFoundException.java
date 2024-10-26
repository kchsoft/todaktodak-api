package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import java.time.LocalDate;

public class DiaryNotFoundException extends DiaryException {

  public DiaryNotFoundException(DiaryErrorSpec errorSpec, Long memberId, LocalDate diaryDate) {
    super(errorSpec, memberId, diaryDate);
  }
}
