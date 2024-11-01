package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.storage.S3FileStorageService;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.dto.PublicDiary;
import com.heartsave.todaktodak_api.diary.dto.PublicDiaryContentlyOnly;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.diary.dto.response.PublicDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.DiaryReactionEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentOnlyProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PublicDiaryService {
  private final DiaryRepository diaryRepository;
  private final PublicDiaryRepository publicDiaryRepository;
  private final DiaryReactionRepository diaryReactionRepository;
  private final S3FileStorageService s3FileStorageService;

  public PublicDiaryPaginationResponse getPublicDiaryPagination(
      TodakUser principal, Long publicDiaryId) { // Todo : 쿼리 최적화
    log.info("공개 일기 content only를 조회합니다.");
    List<PublicDiaryContentlyOnly> manyOfContentOnly = getContentOnly(publicDiaryId);
    log.info("공개 일기 content only 조회를 마쳤습니다.");

    Long memberId = principal.getId();
    PublicDiaryPaginationResponse response = new PublicDiaryPaginationResponse();

    log.info("공개 일기 reaction을 조회합니다.");
    for (PublicDiaryContentlyOnly contentOnly : manyOfContentOnly)
      response.addPublicDiary(getPublicDiary(memberId, contentOnly));
    log.info("공개 일기 reaction 조회를 마칩니다.");
    return response;
  }

  private List<PublicDiaryContentlyOnly> getContentOnly(Long publicDiaryId) {
    List<PublicDiaryContentOnlyProjection> projections = getViewsProjection(publicDiaryId);
    List<PublicDiaryContentlyOnly> views = new ArrayList<>();
    for (PublicDiaryContentOnlyProjection projection : projections) {
      List<String> webtoonUrls =
          s3FileStorageService.preSignedWebtoonUrlFrom(projection.getWebtoonImageUrls());
      String characterImageUrl =
          s3FileStorageService.preSignedCharacterImageUrlFrom(projection.getCharacterImageUrl());
      String bgmUrl = s3FileStorageService.preSignedBgmUrlFrom(projection.getBgmUrl());
      PublicDiaryContentlyOnly view =
          new PublicDiaryContentlyOnly(projection, webtoonUrls, characterImageUrl, bgmUrl);
      views.add(view);
    }
    return views;
  }

  private List<PublicDiaryContentOnlyProjection> getViewsProjection(Long publicDiaryId) {
    if (publicDiaryId == 0) {
      Long latestId = publicDiaryRepository.findLatestId().get();
      publicDiaryId = latestId + 1;
    }
    return publicDiaryRepository.findViewsById(publicDiaryId, PageRequest.of(0, 5));
  }

  private PublicDiary getPublicDiary(Long memberId, PublicDiaryContentlyOnly content) {
    DiaryReactionCountProjection reactionCount =
        diaryReactionRepository.countEachByDiaryId(content.getDiaryId()).get();
    List<DiaryReactionType> memberReaction =
        diaryReactionRepository.findReactionByMemberAndDiaryId(memberId, content.getDiaryId());
    return new PublicDiary(content, reactionCount, memberReaction);
  }

  public void write(TodakUser principal, String publicContent, Long diaryId) {
    Long memberId = principal.getId();
    DiaryEntity diary =
        diaryRepository
            .findById(diaryId) // Todo : exist 로 최적화
            .orElseThrow(
                () ->
                    new DiaryNotFoundException(DiaryErrorSpec.DIARY_NOT_FOUND, memberId, diaryId));
    PublicDiaryEntity publicDiary =
        PublicDiaryEntity.builder()
            .diaryEntity(diary)
            .memberEntity(diary.getMemberEntity())
            .publicContent(publicContent)
            .build();
    publicDiaryRepository.save(publicDiary);
  }

  public void toggleReactionStatus(TodakUser principal, PublicDiaryReactionRequest request) {
    Long memberId = principal.getId();
    Long diaryId = request.diaryId();
    DiaryReactionType reactionType = request.reactionType();
    DiaryReactionEntity reactionEntity = getDiaryReactionEntity(memberId, diaryId, reactionType);
    try {
      diaryReactionRepository.save(reactionEntity); // Todo: Optimistic Lock , Pessimistic Lock 학습
    } catch (DataIntegrityViolationException e) {
      diaryReactionRepository.deleteByMemberIdAndDiaryIdAndReactionType(
          memberId, diaryId, reactionType);
    }
  }

  private DiaryReactionEntity getDiaryReactionEntity(
      Long memberId, Long diaryId, DiaryReactionType reactionType) {
    DiaryReactionEntity reactionEntity =
        DiaryReactionEntity.builder()
            .memberEntity(MemberEntity.createById(memberId))
            .diaryEntity(DiaryEntity.createById(diaryId))
            .reactionType(reactionType)
            .build();
    return reactionEntity;
  }
}
