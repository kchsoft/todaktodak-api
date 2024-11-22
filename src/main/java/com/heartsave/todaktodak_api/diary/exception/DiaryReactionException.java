package com.heartsave.todaktodak_api.diary.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorField;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;

public abstract class DiaryReactionException extends BaseException {

  protected DiaryReactionException(ErrorSpec errorSpec, ErrorField errorField) {
    super(errorSpec, errorField);
  }
}
