package com.heartsave.todaktodak_api.common.storage.s3.expcetion;

import com.heartsave.todaktodak_api.common.exception.ErrorFieldBuilder;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;

public class InvalidS3UrlException extends S3Exception {

  public InvalidS3UrlException(ErrorSpec errorSpec, String url) {
    super(errorSpec, ErrorFieldBuilder.builder().add("s3Url", url).build());
  }
}
