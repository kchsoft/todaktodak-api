package com.heartsave.todaktodak_api.domain.ai.callback.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.member.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.domain.ai.callback.domain.AiCallbackBgmCompletion;
import com.heartsave.todaktodak_api.domain.ai.callback.domain.AiCallbackWebtoonCompletion;
import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackBgmRequest;
import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackWebtoonRequest;
import com.heartsave.todaktodak_api.domain.ai.callback.repository.AiCallbackRepository;
import com.heartsave.todaktodak_api.domain.event.constant.EventType;
import com.heartsave.todaktodak_api.domain.event.entity.EventEntity;
import com.heartsave.todaktodak_api.domain.event.service.EventService;
import com.heartsave.todaktodak_api.domain.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AiDiaryService {

  private final AiCallbackRepository aiRepository;
  private final S3FileStorageManager s3FileStorageManager;
  private final EventService eventService;
  private final MemberRepository memberRepository;

  public void saveWebtoon(AiCallbackWebtoonRequest request) {
    String keyUrl = s3FileStorageManager.parseKeyFrom(request.webtoonFolderUrl());
    AiCallbackWebtoonCompletion completion = AiCallbackWebtoonCompletion.from(request, keyUrl);

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

  public void saveBgm(AiCallbackBgmRequest request) {
    String keyUrl = s3FileStorageManager.parseKeyFrom(request.bgmUrl());
    AiCallbackBgmCompletion completion = AiCallbackBgmCompletion.from(request, keyUrl);

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
