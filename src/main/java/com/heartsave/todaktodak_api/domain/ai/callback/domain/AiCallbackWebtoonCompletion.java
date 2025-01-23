package com.heartsave.todaktodak_api.domain.ai.callback.domain;

import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackWebtoonRequest;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AiCallbackWebtoonCompletion {
  private final Long memberId;
  private final Instant createdDate;
  private final String keyUrl;

  public static AiCallbackWebtoonCompletion from(AiCallbackWebtoonRequest request, String keyUrl) {
    return new AiCallbackWebtoonCompletion(request.memberId(), request.createdDate(), keyUrl);
  }
}
