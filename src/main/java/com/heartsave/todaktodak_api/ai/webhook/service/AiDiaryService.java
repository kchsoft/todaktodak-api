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
    int result = aiRepository.updateWebtoonUrl(request);
    if (result == 0) {
      log.warn("Webtoon Url을 업데이트 할 일기가 없습니다. memberId={}, diaryDate={}", memberId, createdDate);
      return;
    }
    if (aiRepository.isContentCompleted(memberId, createdDate)) { // Todo : Lock 설정을 통한 동시성 통제
      // Todo : SSE 알림 구현
    }
  }
}
