package com.heartsave.todaktodak_api.ai.webhook.service;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiBgmRequest;
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
    log.info("webtoon url 업데이트를 시작합니다.");
    int result = aiRepository.updateWebtoonUrl(request);
    if (result == 0) {
      log.warn("Webtoon Url을 업데이트 할 일기가 없습니다. memberId={}, diaryDate={}", memberId, createdDate);
      return;
    }
    log.info("webtoon url 업데이트를 마쳤습니다.");
    if (aiRepository.isContentCompleted(memberId, createdDate)) { // Todo : Lock 설정을 통한 동시성 통제
      // Todo : SSE 알림 구현
    }
  }

  public void saveBgm(AiBgmRequest request) {
    Long memberId = request.memberId();
    LocalDate createdDate = request.createdDate();

    log.info("Bgm url 업데이트를 시작합니다. memberId={}", memberId);
    int result = aiRepository.updateBgmUrl(request);
    if (result == 0) {
      log.warn("bgm Url을 업데이트 할 일기가 없습니다. memberId={}, diaryDate={}", memberId, createdDate);
      return;
    }
    log.info("Bgm url 업데이트를 마쳤습니다. memberId={}", memberId);
    if (aiRepository.isContentCompleted(memberId, createdDate)) { // Todo : Lock 설정을 통한 동시성 통제
      // Todo : SSE 알림 구현
    }
  }
}
