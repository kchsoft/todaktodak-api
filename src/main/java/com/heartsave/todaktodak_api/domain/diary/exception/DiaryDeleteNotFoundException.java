package com.heartsave.todaktodak_api.domain.diary.exception;

import com.heartsave.todaktodak_api.common.exception.errorspec.diary.DiaryErrorSpec;

public class DiaryDeleteNotFoundException extends DiaryException {

  public DiaryDeleteNotFoundException(DiaryErrorSpec errorSpec, Long memberId, Long diaryId) {
    super(errorSpec, memberId, diaryId);
  }
}
