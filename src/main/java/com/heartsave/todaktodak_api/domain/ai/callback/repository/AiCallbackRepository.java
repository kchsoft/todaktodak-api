package com.heartsave.todaktodak_api.domain.ai.callback.repository;

import com.heartsave.todaktodak_api.domain.ai.callback.domain.AiCallbackBgmCompletion;
import com.heartsave.todaktodak_api.domain.ai.callback.domain.AiCallbackWebtoonCompletion;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AiCallbackRepository {

  private final AiJpaRepository jpaRepository;

  public int updateWebtoonUrl(AiCallbackWebtoonCompletion completion) {
    return jpaRepository.updateWebtoonUrl(
        completion.getMemberId(), completion.getCreatedDate(), completion.getKeyUrl());
  }

  public int updateBgmUrl(AiCallbackBgmCompletion completion) {
    return jpaRepository.updateBgmUrl(
        completion.getMemberId(), completion.getCreatedDate(), completion.getKeyUrl());
  }

  public Boolean isContentCompleted(Long memberId, Instant createdDate) {
    return jpaRepository.isContentCompleted(memberId, createdDate).orElse(false);
  }
}
