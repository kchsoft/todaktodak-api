package com.heartsave.todaktodak_api.ai.exception;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorFieldBuilder;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;

public class AiException extends BaseException {
  public AiException(ErrorSpec errorSpec, String fileName) {
    super(errorSpec, ErrorFieldBuilder.builder().add("fileName", fileName).build());
  }
}
