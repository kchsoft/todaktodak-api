package com.heartsave.todaktodak_api.common.exception.errorspec;

import org.springframework.http.HttpStatus;

public interface ErrorSpec {
  HttpStatus getStatus();

  String getCode();

  String getClientMessage();

  String getDebugMessage();

  String name();
}
