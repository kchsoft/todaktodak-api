package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.ai.client.dto.response.AiDiaryContentResponse;
import com.heartsave.todaktodak_api.ai.client.service.AiClientService;
import com.heartsave.todaktodak_api.common.converter.InstantUtils;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryResponse;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryYearMonthResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryYearMonthProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryDailyWritingLimitExceedException;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DiaryService {

  private final AiClientService aiClientService;
  private final DiaryRepository diaryRepository;
  private final MemberRepository memberRepository;
  private final S3FileStorageManager s3FileStorageManager;

  public DiaryWriteResponse write(Long memberId, DiaryWriteRequest request, String zoneName) {

    DiaryEntity diary = createDiaryEntity(memberId, request);
    validateDailyDiaryLimit(memberId, request, zoneName);

    log.info("AI 컨텐츠 생성 요청을 시작합니다.");
    AiDiaryContentResponse response = aiClientService.callDiaryContent(diary);
    log.info("AI 컨텐츠 생성 요청을 마쳤습니다.");

    log.info("DB에 일기 저장을 요청합니다.");
    diary.addAiContent(response);
    diaryRepository.save(diary);
    log.info("DB에 일기를 저장하였습니다.");
    return DiaryWriteResponse.builder().aiComment(response.getAiComment()).build();
  }

  private void validateDailyDiaryLimit(Long memberId, DiaryWriteRequest request, String zoneName) {
    Instant diaryCreatedTime = request.getCreatedTime();
    if (diaryRepository.existsByMemberEntity_IdAndDiaryCreatedTimeBetween(
        memberId,
        InstantUtils.toDayStartAtZone(diaryCreatedTime, zoneName),
        InstantUtils.toDayEndAtZone(diaryCreatedTime, zoneName))) {
      throw new DiaryDailyWritingLimitExceedException(
          DiaryErrorSpec.DAILY_WRITING_LIMIT_EXCEED, memberId);
    }
  }

  public void delete(Long memberId, Long diaryId) {
    log.info("DB에 일기를 삭제를 요청합니다.");
    DiaryEntity diary =
        diaryRepository
            .findById(diaryId)
            .orElseThrow(
                () ->
                    new DiaryNotFoundException(DiaryErrorSpec.DIARY_NOT_FOUND, memberId, diaryId));
    diaryRepository.delete(diary);
    log.info("DB에서 일기를 삭제했습니다.");
    s3FileStorageManager.deleteDiaryContents(
        List.of(diary.getWebtoonImageUrl(), diary.getBgmUrl()));
  }

  @Transactional(readOnly = true)
  public DiaryYearMonthResponse getYearMonth(Long memberId, Instant request, String zoneName) {
    Instant startTime = InstantUtils.toMonthStartAtZone(request, zoneName);
    Instant endTime = InstantUtils.toMonthEndAtZone(request, zoneName);
    log.info("해당 연월에 작성한 일기를 정보를 요청합니다.");
    List<DiaryYearMonthProjection> yearMonthProjection =
        diaryRepository.findByMemberEntity_IdAndDiaryCreatedTimeBetweenOrderByDiaryCreatedTimeDesc(
            memberId, startTime, endTime);

    log.info("해당 연월에 작성한 일기를 정보를 성공적으로 가져왔습니다.");
    return DiaryYearMonthResponse.builder().diaryYearMonths(yearMonthProjection).build();
  }

  @Transactional(readOnly = true)
  public DiaryResponse getDiary(Long memberId, Instant request, String zoneName) {
    log.info("사용자의 나의 일기 정보를 요청합니다.");

    DiaryEntity diary =
        diaryRepository
            .findDiaryEntityByMemberEntity_IdAndDiaryCreatedTimeBetween(
                memberId,
                InstantUtils.toDayStartAtZone(request, zoneName),
                InstantUtils.toDayEndAtZone(request, zoneName))
            .orElseThrow(
                () ->
                    new DiaryNotFoundException(DiaryErrorSpec.DIARY_NOT_FOUND, memberId, request));

    log.info("사용자의 나의 일기 정보를 성공적으로 가져왔습니다.");

    return DiaryResponse.builder()
        .diaryId(diary.getId())
        .emotion(diary.getEmotion())
        .content(diary.getContent())
        .webtoonImageUrls(
            s3FileStorageManager.preSignedWebtoonUrlFrom(List.of(diary.getWebtoonImageUrl())))
        .bgmUrl(s3FileStorageManager.preSignedBgmUrlFrom(diary.getBgmUrl()))
        .aiComment(diary.getAiComment())
        .date(diary.getDiaryCreatedTime())
        .build();
  }

  private DiaryEntity createDiaryEntity(Long memberId, DiaryWriteRequest request) {
    MemberEntity member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, memberId));
    return DiaryEntity.createDefault(request, member);
  }
}
