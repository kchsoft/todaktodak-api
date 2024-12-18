package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.PublicDiaryErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryResponse;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryContentProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.factory.DiaryPageIndexFactory;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.MySharedDiaryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MySharedDiaryService {

  private final MySharedDiaryRepository mySharedDiaryRepository;
  private final DiaryReactionRepository reactionRepository;
  private final S3FileStorageManager s3FileStorageManager;
  private final DiaryPageIndexFactory indexFactory;

  @Transactional(readOnly = true)
  public MySharedDiaryPaginationResponse getPage(Long memberId, DiaryPageRequest request) {
    log.info("나의 공개된 일기 정보를 요청합니다.");
    DiaryPageIndex pageIndex = indexFactory.createFrom(request, memberId);
    List<MySharedDiaryPreviewProjection> previews = fetchPreviews(memberId, pageIndex);
    replaceWithPreSignedUrls(previews);
    log.info("나의 공개된 일기 정보 요청을 성공적으로 마쳤습니다.");
    return MySharedDiaryPaginationResponse.of(previews);
  }

  private List<MySharedDiaryPreviewProjection> fetchPreviews(
      Long memberId, DiaryPageIndex pageIndex) {
    log.info("나의 공개된 일기 preview 정보를 조회합니다.");
    return mySharedDiaryRepository.findNextPreviews(memberId, pageIndex, PageRequest.of(0, 12));
  }

  private void replaceWithPreSignedUrls(List<MySharedDiaryPreviewProjection> previews) {
    log.info("나의 공개된 일기 이미지 URL pre-signed 과정을 시작합니다.");
    for (MySharedDiaryPreviewProjection preview : previews) {
      preview.replaceWebtoonImageUrl(
          s3FileStorageManager.preSignedFirstWebtoonUrlFrom(preview.getWebtoonImageUrl()));
    }
  }

  @Transactional(readOnly = true)
  public MySharedDiaryResponse getDiary(Long memberId, Instant requestInstant) {
    log.info("나의 공개된 일기 상세 정보를 요청합니다.");
    MySharedDiaryContentProjection contentOnly = fetchContent(requestInstant, memberId);
    replaceWithPreSignedUrls(contentOnly);

    DiaryReactionCountProjection reactionCount =
        reactionRepository.countEachByPublicDiaryId(contentOnly.getDiaryId());
    List<DiaryReactionType> memberReaction =
        reactionRepository.findMemberReaction(memberId, contentOnly.getDiaryId());
    log.info("나의 공개된 일기 상세 정보 요청을 성공적으로 마쳤습니다.");
    return MySharedDiaryResponse.of(contentOnly, reactionCount, memberReaction);
  }

  private void replaceWithPreSignedUrls(MySharedDiaryContentProjection contentProjection) {
    log.info("나의 공개된 일기 URL pre-signed 과정을 시작합니다.");
    contentProjection.replaceWebtoonImageUrls(
        s3FileStorageManager.preSignedWebtoonUrlFrom(contentProjection.getWebtoonImageUrls()));
    contentProjection.replaceBgmUrl(
        s3FileStorageManager.preSignedBgmUrlFrom(contentProjection.getBgmUrl()));
  }

  private MySharedDiaryContentProjection fetchContent(Instant requestDateTime, Long memberId) {
    log.info("나의 공개된 일기 content only 를 요청합니다.");
    MySharedDiaryContentProjection contentProjection =
        mySharedDiaryRepository
            .findContent(memberId, requestDateTime)
            .orElseThrow(
                () ->
                    new PublicDiaryNotFoundException(
                        PublicDiaryErrorSpec.PUBLIC_DIARY_NOT_FOUND, requestDateTime));

    return contentProjection;
  }

  public void delete(Long memberId, Long publicDiaryId) {
    PublicDiaryEntity publicDiary =
        mySharedDiaryRepository
            .findById(publicDiaryId)
            .orElseThrow(
                () ->
                    new PublicDiaryNotFoundException(
                        PublicDiaryErrorSpec.PUBLIC_DIARY_NOT_FOUND, publicDiaryId));

    if (!Objects.equals(memberId, publicDiary.getMemberEntity().getId())) {
      throw new PublicDiaryNotFoundException(
          PublicDiaryErrorSpec.PUBLIC_DIARY_DELETE_NOT_FOUND, publicDiaryId);
    }
    mySharedDiaryRepository.delete(publicDiary);
  }
}
