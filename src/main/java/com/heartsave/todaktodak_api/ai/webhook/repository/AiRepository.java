package com.heartsave.todaktodak_api.ai.webhook.repository;

import com.heartsave.todaktodak_api.ai.webhook.domain.WebhookBgmCompletion;
import com.heartsave.todaktodak_api.ai.webhook.domain.WebhookWebtoonCompletion;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AiRepository {

  private final AiJpaRepository jpaRepository;

  public int updateWebtoonUrl(WebhookWebtoonCompletion completion) {
    return jpaRepository.updateWebtoonUrl(
        completion.getMemberId(), completion.getCreatedDate(), completion.getKeyUrl());
  }

  public int updateBgmUrl(WebhookBgmCompletion completion) {
    return jpaRepository.updateBgmUrl(
        completion.getMemberId(), completion.getCreatedDate(), completion.getKeyUrl());
  }

  public Boolean isContentCompleted(Long memberId, Instant createdDate) {
    return jpaRepository.isContentCompleted(memberId, createdDate).orElse(false);
  }
}
