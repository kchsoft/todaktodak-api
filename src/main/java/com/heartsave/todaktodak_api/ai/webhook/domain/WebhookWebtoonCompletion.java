package com.heartsave.todaktodak_api.ai.webhook.domain;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookWebtoonCompletionRequest;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class WebhookWebtoonCompletion {
  private final Long memberId;
  private final LocalDate createdDate;
  private final String keyUrl;

  public static WebhookWebtoonCompletion from(
      WebhookWebtoonCompletionRequest request, String keyUrl) {
    return new WebhookWebtoonCompletion(request.memberId(), request.createdDate(), keyUrl);
  }
}
