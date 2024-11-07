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

  public int updateWebtoonUrl(WebhookWebtoonCompletionRequest request) {
    return jpaRepository.updateWebtoonUrl(
        request.memberId(), request.createdDate(), request.webtoonFolderUrl());
  }

  public int updateBgmUrl(WebhookBgmCompletionRequest request) {
    return jpaRepository.updateBgmUrl(request.memberId(), request.createdDate(), request.bgmUrl());
  }

  public Boolean isContentCompleted(Long memberId, LocalDate createdDate) {
    return jpaRepository.isContentCompleted(memberId, createdDate).orElse(false);
  }
}
