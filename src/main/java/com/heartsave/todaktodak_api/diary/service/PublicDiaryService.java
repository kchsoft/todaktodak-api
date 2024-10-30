package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

  public PublicDiaryViewDetailResponse get(
      TodakUser principal, Long publicDiaryId) { // Todo : 쿼리 최적화
    Pageable pageable = PageRequest.of(0, 5);
    List<PublicDiaryViewProjection> publicDiaryViews = getPublicDiaryViews(publicDiaryId, pageable);

    Long memberId = principal.getId();
    PublicDiaryViewDetailResponse response = new PublicDiaryViewDetailResponse();
    for (PublicDiaryViewProjection view : publicDiaryViews) {
      response.addViewDetail(getViewDetail(memberId, view));
    }
    return response;
  }

  private List<PublicDiaryViewProjection> getPublicDiaryViews(
      Long publicDiaryId, Pageable pageable) {
    if (publicDiaryId == 0) {
      Long latestId = publicDiaryRepository.findLatestId().get();
      return publicDiaryRepository.findViewsById(latestId + 1, pageable);
    } else {
      return publicDiaryRepository.findViewsById(publicDiaryId, pageable);
    }
  }

  private PublicDiaryViewDetail getViewDetail(Long memberId, PublicDiaryViewProjection view) {
    DiaryReactionCountProjection reactionCount =
        diaryReactionRepository.countEachByDiaryId(view.getDiaryId()).get();
    List<DiaryReactionType> memberReaction =
        diaryReactionRepository.findReactionByMemberId(memberId, view.getDiaryId());
    return PublicDiaryViewDetail.of(view, reactionCount, memberReaction);
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
