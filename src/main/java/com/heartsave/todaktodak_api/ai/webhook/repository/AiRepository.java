package com.heartsave.todaktodak_api.ai.webhook.repository;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiWebtoonRequest;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AiRepository {

  private final AiJpaRepository jpaRepository;

  public int updateWebtoonUrl(AiWebtoonRequest request) {
    return jpaRepository.updateWebtoonUrl(
        request.memberId(), request.createdDate(), request.webtoonFolderUrl());
  }

  public Boolean isContentCompleted(Long memberId, LocalDate createdDate) {
    return jpaRepository.isContentCompleted(memberId, createdDate).orElse(false);
  }
}
