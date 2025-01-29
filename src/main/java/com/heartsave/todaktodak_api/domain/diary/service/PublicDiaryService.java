package com.heartsave.todaktodak_api.domain.diary.service;

import static com.heartsave.todaktodak_api.common.constant.TodakConstant.DIARY.PUBLIC_DIARY_PAGE_SIZE;

import com.heartsave.todaktodak_api.common.exception.errorspec.diary.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.exception.errorspec.diary.PublicDiaryErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.domain.diary.cache.entity.ContentReactionCountEntity;
import com.heartsave.todaktodak_api.domain.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.domain.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.domain.diary.domain.DiaryReactionCount;
import com.heartsave.todaktodak_api.domain.diary.dto.PublicDiary;
import com.heartsave.todaktodak_api.domain.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.domain.diary.dto.response.PublicDiaryPageResponse;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.entity.projection.PublicDiaryContentProjection;
import com.heartsave.todaktodak_api.domain.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.domain.diary.exception.PublicDiaryExistException;
import com.heartsave.todaktodak_api.domain.diary.factory.DiaryPageIndexFactory;
import com.heartsave.todaktodak_api.domain.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.domain.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.domain.diary.repository.PublicDiaryRepository;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
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
        publicDiaryRepository.findNextContents(
            pageIndex, PageRequest.of(0, PUBLIC_DIARY_PAGE_SIZE));
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

    if (diaryRepository.existsById(diaryId) == false)
      throw new DiaryNotFoundException(DiaryErrorSpec.DIARY_NOT_FOUND, memberId, diaryId);

    if (publicDiaryRepository.existsByDiaryEntity_id(diaryId) == true)
      throw new PublicDiaryExistException(
          PublicDiaryErrorSpec.PUBLIC_DIARY_EXIST, memberId, diaryId);

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
