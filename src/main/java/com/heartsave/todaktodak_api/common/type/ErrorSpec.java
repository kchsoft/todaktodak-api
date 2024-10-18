package com.heartsave.todaktodak_api.common.type;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorSpec {
  ;

  private HttpStatus status;
  private String message;
}
