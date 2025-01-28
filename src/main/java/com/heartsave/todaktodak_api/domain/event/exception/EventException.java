package com.heartsave.todaktodak_api.domain.event.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorFieldBuilder;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;

public class EventException extends BaseException {
  public EventException(ErrorSpec errorSpec) {
    super(errorSpec, null);
  }

  public EventException(ErrorSpec errorSpec, Long memberId) {
    super(errorSpec, ErrorFieldBuilder.builder().add("memberId", memberId).build());
  }
}
