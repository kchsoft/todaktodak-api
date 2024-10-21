package com.heartsave.todaktodak_api.common.exception;

import java.util.Map;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class ErrorResponse {
  private String title;
  private Object description;

  private static final String VALIDATION_ERROR = "VALIDATION_ERROR";

  public static ErrorResponse from(ErrorSpec errorSpec) {
    return ErrorResponse.builder()
        .title(errorSpec.name())
        .description(errorSpec.getMessage())
        .build();
  }

  public static ErrorResponse from(Map<String, String> jsonMessage) {
    return ErrorResponse.builder().title(VALIDATION_ERROR).description(jsonMessage).build();
  }
}
