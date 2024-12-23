package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.PublicDiaryErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.diary.cache.entity.ContentReactionCountEntity;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.domain.DiaryReactionCount;
import com.heartsave.todaktodak_api.diary.dto.PublicDiary;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.diary.dto.response.PublicDiaryPageResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIdsProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryExistException;
import com.heartsave.todaktodak_api.diary.factory.DiaryPageIndexFactory;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final DiaryPageIndexFactory pageIndexFactory;
  private final DiaryReactionRepository reactionRepository;
  private final MemberRepository memberRepository;
  private final S3FileStorageManager s3FileStorageManager;
  private final PublicDiaryCacheService publicDiaryCacheService;

  @Transactional(readOnly = true)
  public PublicDiaryPageResponse getPagination(Long memberId, DiaryPageRequest request) {
    DiaryPageIndex pageIndex = pageIndexFactory.createFrom(request);

    List<ContentReactionCountEntity> contentReactionCounts =
        publicDiaryCacheService.getContentReactionCounts(pageIndex);
    if (contentReactionCounts.isEmpty()) {
      log.info("공개 일기 Cache Miss");
      List<PublicDiaryContentProjection> projections = fetchContents(pageIndex);

      for (PublicDiaryContentProjection projection : projections) {
        ContentReactionCountEntity entity = ContentReactionCountEntity.createFrom(projection);
        entity.applyReactionCount(fetchReactionCount(projection.getPublicDiaryId()));
        contentReactionCounts.add(entity);
      }
      publicDiaryCacheService.saveContentReactionCounts(contentReactionCounts);
    }
    updateUrlsWithPreSigned(contentReactionCounts);

    PublicDiaryPageResponse response = new PublicDiaryPageResponse();
    for (ContentReactionCountEntity contentReactionCount : contentReactionCounts) {
      response.addPublicDiary(
          PublicDiary.of(
              contentReactionCount,
              fetchMemberReactions(memberId, contentReactionCount.getPublicDiaryId())));
    }
    return response;
  }

  private List<PublicDiaryContentProjection> fetchContents(DiaryPageIndex pageIndex) {
    List<PublicDiaryContentProjection> nextContents =
        publicDiaryRepository.findNextContents(pageIndex, PageRequest.of(0, 5));
    return nextContents;
  }

  private void updateUrlsWithPreSigned(List<ContentReactionCountEntity> entitys) {
    log.info("공개 일기 content url을 pre-signed url로 변경합니다.");
    for (ContentReactionCountEntity content : entitys) {
      content.updateWebtoonImageUrls(
          s3FileStorageManager.preSignedWebtoonUrlFrom(content.getWebtoonImageUrls()));
      content.updateCharacterImageUrl(
          s3FileStorageManager.preSignedCharacterImageUrlFrom(content.getCharacterImageUrl()));
      content.updateBgmUrl(s3FileStorageManager.preSignedBgmUrlFrom(content.getBgmUrl()));
    }
  }

  private DiaryReactionCount fetchReactionCount(Long publicDiaryId) {
    DiaryReactionCount reactionCount =
        DiaryReactionCount.from(reactionRepository.countEachByPublicDiaryId(publicDiaryId));
    return reactionCount;
  }

  private List<DiaryReactionType> fetchMemberReactions(Long memberId, Long publicDiaryId) {
    List<DiaryReactionType> reactions =
        reactionRepository.findMemberReactions(memberId, publicDiaryId);
    return reactions;
  }

  public void write(Long memberId, Long diaryId, String publicContent) {
    DiaryIdsProjection ids =
        diaryRepository
            .findIdsById(diaryId)
            .orElseThrow(
                () ->
                    new DiaryNotFoundException(DiaryErrorSpec.DIARY_NOT_FOUND, memberId, diaryId));

    if (ids.getPublicDiaryId() != null) {
      throw new PublicDiaryExistException(
          PublicDiaryErrorSpec.PUBLIC_DIARY_EXIST, memberId, ids.getPublicDiaryId());
    }

    DiaryEntity diaryRef = diaryRepository.getReferenceById(diaryId);
    MemberEntity memberRef = memberRepository.getReferenceById(memberId);

    PublicDiaryEntity publicDiary =
        PublicDiaryEntity.builder()
            .diaryEntity(diaryRef)
            .memberEntity(memberRef)
            .publicContent(publicContent)
            .build();
    publicDiaryRepository.save(publicDiary);
  }
}
