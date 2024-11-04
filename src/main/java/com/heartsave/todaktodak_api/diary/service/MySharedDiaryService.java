package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.PublicDiaryErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.storage.S3FileStorageService;
import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryResponse;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryContentOnlyProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.MySharedDiaryRepository;
import java.time.LocalDate;
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
public class MySharedDiaryService {

  private final MySharedDiaryRepository mySharedDiaryRepository;
  private final DiaryReactionRepository reactionRepository;
  private final S3FileStorageService s3FileStorageService;

  public MySharedDiaryPaginationResponse getPagination(TodakUser principal, Long publicDiaryId) {
    log.info("나의 공개된 일기 정보를 요청합니다.");
    Long memberId = principal.getId();
    List<MySharedDiaryPreviewProjection> previews = fetchPreviews(memberId, publicDiaryId);
    replaceWithPreSignedUrls(previews);
    log.info("나의 공개된 일기 정보 요청을 성공적으로 마쳤습니다.");
    return MySharedDiaryPaginationResponse.of(previews);
  }

  private List<MySharedDiaryPreviewProjection> fetchPreviews(Long memberId, Long publicDiaryId) {
    log.info("나의 공개된 일기 preview 정보를 조회합니다.");
    if (publicDiaryId == 0L) publicDiaryId = getFirstPreviewId(memberId); // 공개 일기 조회 API 첫 호출
    return mySharedDiaryRepository.findNextPreviews(memberId, publicDiaryId, PageRequest.of(0, 12));
  }

  private Long getFirstPreviewId(Long memberId) {
    return mySharedDiaryRepository
        .findLatestId(memberId)
        .map(id -> id + 1L)
        .orElseThrow(
            () ->
                new PublicDiaryNotFoundException(PublicDiaryErrorSpec.PUBLIC_DIARY_NOT_FOUND, 0L));
  }

  private void replaceWithPreSignedUrls(List<MySharedDiaryPreviewProjection> previews) {
    log.info("나의 공개된 일기 이미지 URL pre-signed 과정을 시작합니다.");
    for (MySharedDiaryPreviewProjection preview : previews) {
      preview.replaceWebtoonImageUrl(
          s3FileStorageService.preSignedFirstWebtoonUrlFrom(preview.getWebtoonImageUrl()));
    }
  }

  public MySharedDiaryResponse getDiary(TodakUser principal, LocalDate requestDate) {
    log.info("나의 공개된 일기 상세 정보를 요청합니다.");
    Long memberId = principal.getId();
    MySharedDiaryContentOnlyProjection contentOnly = fetchContentOnly(requestDate, memberId);
    replaceWithPreSignedUrls(contentOnly);

    DiaryReactionCountProjection reactionCount =
        reactionRepository.countEachByDiaryId(contentOnly.getDiaryId());
    List<DiaryReactionType> memberReaction =
        reactionRepository.findReactionByMemberAndDiaryId(memberId, contentOnly.getDiaryId());
    log.info("나의 공개된 일기 상세 정보 요청을 성공적으로 마쳤습니다.");
    return MySharedDiaryResponse.of(contentOnly, reactionCount, memberReaction);
  }

  private void replaceWithPreSignedUrls(MySharedDiaryContentOnlyProjection contentOnly) {
    log.info("나의 공개된 일기 URL pre-signed 과정을 시작합니다.");
    contentOnly.replaceWebtoonImageUrls(
        s3FileStorageService.preSignedWebtoonUrlFrom(contentOnly.getWebtoonImageUrls()));
    contentOnly.replaceBgmUrl(s3FileStorageService.preSignedBgmUrlFrom(contentOnly.getBgmUrl()));
  }

  private MySharedDiaryContentOnlyProjection fetchContentOnly(
      LocalDate requestDate, Long memberId) {
    log.info("나의 공개된 일기 content only 를 요청합니다.");
    MySharedDiaryContentOnlyProjection contentOnly =
        mySharedDiaryRepository
            .findContentOnly(memberId, requestDate)
            .orElseThrow(
                () ->
                    new PublicDiaryNotFoundException(
                        PublicDiaryErrorSpec.PUBLIC_DIARY_NOT_FOUND, requestDate));
    return contentOnly;
  }
}
