package com.heartsave.todaktodak_api.common.exception;

import com.heartsave.todaktodak_api.common.exception.errorspec.ErrorSpec;
import java.util.Map;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class ErrorResponse {
  private String title;
  private Object message;

  private static final String VALIDATION_ERROR = "VALIDATION_ERROR";

  public static ErrorResponse from(ErrorSpec errorSpec) {
    return ErrorResponse.builder()
        .title(errorSpec.name())
        .message(errorSpec.getClientMessage())
        .build();
  }

  public static ErrorResponse from(Map<String, String> jsonMessage) {
    return ErrorResponse.builder().title(VALIDATION_ERROR).message(jsonMessage).build();
  }
}
