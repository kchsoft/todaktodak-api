package com.heartsave.todaktodak_api.domain.ai.callback.domain;

import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackBgmRequest;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AiCallbackBgmCompletion {
  private final Long memberId;
  private final Instant createdDate;
  private final String keyUrl;

  public static AiCallbackBgmCompletion from(AiCallbackBgmRequest request, String keyUrl) {
    return new AiCallbackBgmCompletion(request.memberId(), request.createdDate(), keyUrl);
  }
}
