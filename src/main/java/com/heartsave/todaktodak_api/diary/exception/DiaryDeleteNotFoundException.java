package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;

public class DiaryDeleteNotFoundException extends DiaryException {

  public DiaryDeleteNotFoundException(DiaryErrorSpec errorSpec, Long memberId, Long diaryId) {
    super(errorSpec, memberId, diaryId);
  }
}
