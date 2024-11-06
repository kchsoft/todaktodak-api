package com.heartsave.todaktodak_api.ai.webhook.service;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiWebtoonRequest;
import com.heartsave.todaktodak_api.ai.webhook.repository.AiRepository;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
public class AiDiaryService {

  AiRepository aiRepository;

  public void saveWebtoon(AiWebtoonRequest request) {
    Long memberId = request.memberId();
    LocalDate createdDate = request.createdDate();
    aiRepository.updateWebtoonUrl(memberId, createdDate, request.webtoonFolderUrl());
    if (!aiRepository.isDefaultBgmUrl(memberId, createdDate)) { // Todo : Lock 설정을 통한 동시성 통제
      // Todo : SSE 알림 구현
    }
  }
}
