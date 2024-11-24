package com.heartsave.todaktodak_api.auth.exception;

import com.heartsave.todaktodak_api.auth.dto.request.SignUpRequest;
import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.exception.ErrorField;
import com.heartsave.todaktodak_api.common.exception.ErrorFieldBuilder;
import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;

public final class AuthException extends BaseException {
  public AuthException(ErrorSpec errorSpec) {
    super(errorSpec, ErrorFieldBuilder.builder().build());
  }

  public AuthException(ErrorSpec errorSpec, ErrorField errorField) {
    super(errorSpec, errorField);
  }

  public AuthException(ErrorSpec errorSpec, SignUpRequest dto) {
    super(
        errorSpec,
        ErrorFieldBuilder.builder()
            .add("email", dto.email())
            .add("nickname", dto.nickname())
            .add("loginId", dto.loginId())
            .build());
  }
}
