package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.storage.S3FileStorageService;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.dto.PublicDiaryView;
import com.heartsave.todaktodak_api.diary.dto.PublicDiaryViewDetail;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryReactionRequest;
import com.heartsave.todaktodak_api.diary.dto.response.PublicDiaryViewDetailResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.DiaryReactionEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryViewProjection;
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

  public PublicDiaryViewDetailResponse getPublicDiaryViewDetail(
      TodakUser principal, Long publicDiaryId) { // Todo : 쿼리 최적화
    log.info("공개 일기 view를 조회합니다.");
    List<PublicDiaryView> diaryViews = getDiaryViews(publicDiaryId);
    log.info("공개 일기 view 조회를 마쳤습니다.");

    Long memberId = principal.getId();
    PublicDiaryViewDetailResponse response = new PublicDiaryViewDetailResponse();

    log.info("공개 일기 reaction을 조회합니다.");
    for (PublicDiaryView diaryView : diaryViews)
      response.addViewDetail(getViewDetail(memberId, diaryView));
    log.info("공개 일기 reaction 조회를 마칩니다.");
    return response;
  }

  private List<PublicDiaryView> getDiaryViews(Long publicDiaryId) {
    List<PublicDiaryViewProjection> projections = getViewsProjection(publicDiaryId);
    List<PublicDiaryView> views = new ArrayList<>();
    for (PublicDiaryViewProjection projection : projections) {
      List<String> webtoonUrls =
          s3FileStorageService.preSignedWebtoonUrlFrom(projection.getWebtoonImageUrl());
      String characterImageUrl =
          s3FileStorageService.preSignedCharacterImageUrlFrom(projection.getCharacterImageUrl());
      String bgmUrl = s3FileStorageService.preSignedBgmUrlFrom(projection.getBgmUrl());
      PublicDiaryView view =
          new PublicDiaryView(projection, webtoonUrls, characterImageUrl, bgmUrl);
      views.add(view);
    }
    return views;
  }

  private List<PublicDiaryViewProjection> getViewsProjection(Long publicDiaryId) {
    if (publicDiaryId == 0) {
      Long latestId = publicDiaryRepository.findLatestId().get();
      publicDiaryId = latestId + 1;
    }
    return publicDiaryRepository.findViewsById(publicDiaryId, PageRequest.of(0, 5));
  }

  private PublicDiaryViewDetail getViewDetail(Long memberId, PublicDiaryView view) {
    DiaryReactionCountProjection reactionCount =
        diaryReactionRepository.countEachByDiaryId(view.getDiaryId()).get();
    List<DiaryReactionType> memberReaction =
        diaryReactionRepository.findReactionByMemberAndDiaryId(memberId, view.getDiaryId());
    return new PublicDiaryViewDetail(view, reactionCount, memberReaction);
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
