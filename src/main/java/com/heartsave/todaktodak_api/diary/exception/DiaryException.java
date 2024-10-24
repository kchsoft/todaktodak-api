package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import lombok.Getter;

@Getter
public abstract class DiaryException extends BaseException {

  protected DiaryException(DiaryErrorSpec errorSpec, Long memberId, Long diaryId) {
    super(errorSpec);
    getErrorField().add("memberId", memberId).add("diaryId", diaryId);
  }

  protected DiaryException(DiaryErrorSpec errorSpec, Long memberId) {
    super(errorSpec);
    getErrorField().add("memberId", memberId);
  }
}
