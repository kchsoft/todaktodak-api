package com.heartsave.todaktodak_api.common.storage.s3.expcetion;

import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorField;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;

public abstract class S3Exception extends BaseException {
  protected S3Exception(ErrorSpec errorSpec, ErrorField errorField) {
    super(errorSpec, errorField);
  }
}
