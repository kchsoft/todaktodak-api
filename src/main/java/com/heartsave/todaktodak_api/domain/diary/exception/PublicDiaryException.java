package com.heartsave.todaktodak_api.domain.diary.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorField;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;

public abstract class PublicDiaryException extends BaseException {

  protected PublicDiaryException(ErrorSpec errorSpec, ErrorField errorField) {
    super(errorSpec, errorField);
  }
}
