package com.heartsave.todaktodak_api.ai.webhook.repository;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookBgmCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookWebtoonCompletionRequest;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AiRepository {

  private final AiJpaRepository jpaRepository;

  public int updateWebtoonUrl(WebhookWebtoonCompletionRequest request, String url) {
    return jpaRepository.updateWebtoonUrl(request.memberId(), request.createdDate(), url);
  }

  public int updateBgmUrl(WebhookBgmCompletionRequest request,String url) {
    return jpaRepository.updateBgmUrl(request.memberId(), request.createdDate(), url);
  }

  public Boolean isContentCompleted(Long memberId, LocalDate createdDate) {
    return jpaRepository.isContentCompleted(memberId, createdDate).orElse(false);
  }
}
