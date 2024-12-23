package com.heartsave.todaktodak_api.ai.webhook.domain;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookBgmCompletionRequest;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class WebhookBgmCompletion {
  private final Long memberId;
  private final Instant createdDate;
  private final String keyUrl;

  public static WebhookBgmCompletion from(WebhookBgmCompletionRequest request, String keyUrl) {
    return new WebhookBgmCompletion(request.memberId(), request.createdDate(), keyUrl);
  }
}
