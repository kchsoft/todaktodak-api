package com.heartsave.todaktodak_api.ai.webhook.service;

import com.heartsave.todaktodak_api.ai.webhook.domain.WebhookBgmCompletion;
import com.heartsave.todaktodak_api.ai.webhook.domain.WebhookWebtoonCompletion;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookBgmCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookWebtoonCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.repository.AiRepository;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.event.constant.EventType;
import com.heartsave.todaktodak_api.event.entity.EventEntity;
import com.heartsave.todaktodak_api.event.service.EventService;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
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
  private final S3FileStorageManager s3FileStorageManager;
  private final EventService eventService;
  private final MemberRepository memberRepository;

  public void saveWebtoon(WebhookWebtoonCompletionRequest request) {
    String keyUrl = s3FileStorageManager.parseKeyFrom(request.webtoonFolderUrl());
    WebhookWebtoonCompletion completion = WebhookWebtoonCompletion.from(request, keyUrl);

    log.info("webtoon url 업데이트를 시작합니다. {}", request.webtoonFolderUrl());
    if (aiRepository.updateWebtoonUrl(completion) == 0) {
      log.warn(
          "Webtoon Url을 업데이트 할 일기가 없습니다. memberId={}, diaryDate={}",
          completion.getMemberId(),
          completion.getCreatedDate());
      return;
    }

    log.info("webtoon url 업데이트를 마쳤습니다. memberId={}", completion.getMemberId());
    if (aiRepository.isContentCompleted(
        completion.getMemberId(), completion.getCreatedDate())) { // Todo : Lock 설정을 통한 동시성 통제
      notify(completion.getMemberId());
    }
  }

  public void saveBgm(WebhookBgmCompletionRequest request) {
    String keyUrl = s3FileStorageManager.parseKeyFrom(request.bgmUrl());
    WebhookBgmCompletion completion = WebhookBgmCompletion.from(request, keyUrl);

    log.info("Bgm url 업데이트를 시작합니다. memberId={}", completion.getMemberId());
    if (aiRepository.updateBgmUrl(completion) == 0) {
      log.warn(
          "bgm Url을 업데이트 할 일기가 없습니다. memberId={}, diaryDate={}",
          completion.getMemberId(),
          completion.getCreatedDate());
      return;
    }
    log.info("Bgm url 업데이트를 마쳤습니다. memberId={}", completion.getMemberId());
    if (aiRepository.isContentCompleted(
        completion.getMemberId(), completion.getCreatedDate())) { // Todo : Lock 설정을 통한 동시성 통제
      notify(completion.getMemberId());
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
