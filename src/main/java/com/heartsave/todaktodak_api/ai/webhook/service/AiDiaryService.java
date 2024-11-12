package com.heartsave.todaktodak_api.ai.webhook.service;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookBgmCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookWebtoonCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.repository.AiRepository;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageService;
import com.heartsave.todaktodak_api.event.constant.EventType;
import com.heartsave.todaktodak_api.event.entity.EventEntity;
import com.heartsave.todaktodak_api.event.service.EventService;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AiDiaryService {

  private final AiRepository aiRepository;
  private final S3FileStorageService s3FileStorageService;
  private final EventService eventService;
  private final MemberRepository memberRepository;

  public void saveWebtoon(WebhookWebtoonCompletionRequest request) {
    Long memberId = request.memberId();
    LocalDate createdDate = request.createdDate();
    log.info("webtoon url 업데이트를 시작합니다. {}", request.webtoonFolderUrl());
    String keyUrl = s3FileStorageService.parseKeyFrom(request.webtoonFolderUrl());
    int result = aiRepository.updateWebtoonUrl(request, keyUrl);
    if (result == 0) {
      log.warn("Webtoon Url을 업데이트 할 일기가 없습니다. memberId={}, diaryDate={}", memberId, createdDate);
      return;
    }
    log.info("webtoon url 업데이트를 마쳤습니다.");
    if (aiRepository.isContentCompleted(memberId, createdDate)) { // Todo : Lock 설정을 통한 동시성 통제
      notify(memberId);
    }
  }

  public void saveBgm(WebhookBgmCompletionRequest request) {
    Long memberId = request.memberId();
    LocalDate createdDate = request.createdDate();
    String keyUrl = s3FileStorageService.parseKeyFrom(request.bgmUrl());
    log.info("Bgm url 업데이트를 시작합니다. memberId={}", memberId);
    int result = aiRepository.updateBgmUrl(request, keyUrl);
    if (result == 0) {
      log.warn("bgm Url을 업데이트 할 일기가 없습니다. memberId={}, diaryDate={}", memberId, createdDate);
      return;
    }
    log.info("Bgm url 업데이트를 마쳤습니다. memberId={}", memberId);
    if (aiRepository.isContentCompleted(memberId, createdDate)) { // Todo : Lock 설정을 통한 동시성 통제
      notify(memberId);
    }
  }

  private void notify(Long memberId) {
    var member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, memberId));
    eventService.send(
        EventEntity.builder()
            .memberEntity(member)
            .eventName(EventType.DIARY.getType())
            .eventData("일기가 생성됐습니다.")
            .build());
  }
}
