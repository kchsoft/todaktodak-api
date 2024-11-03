package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.common.exception.errorspec.PublicDiaryErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.storage.S3FileStorageService;
import com.heartsave.todaktodak_api.diary.dto.response.MySharedDiaryPaginationResponse;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import com.heartsave.todaktodak_api.diary.exception.PublicDiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.repository.MySharedDiaryRepository;
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
  private final S3FileStorageService s3FileStorageService;

  public MySharedDiaryPaginationResponse getPagination(TodakUser principal, Long publicDiaryId) {
    Long memberId = principal.getId();
    List<MySharedDiaryPreviewProjection> previews = fetchPreviews(memberId, publicDiaryId);
    replaceWithPreSignedUrls(previews);
    return MySharedDiaryPaginationResponse.of(previews);
  }

  private List<MySharedDiaryPreviewProjection> fetchPreviews(Long memberId, Long publicDiaryId) {
    log.info("나의 공개된 일기 preview 정보를 조회합니다.");
    if (publicDiaryId == 0L) publicDiaryId = getMaxId(memberId); // 공개 일기 조회 API 첫 호출
    return mySharedDiaryRepository.findNextPreviews(memberId, publicDiaryId, PageRequest.of(0, 12));
  }

  private Long getMaxId(Long memberId) {
    return mySharedDiaryRepository
        .findLatestId(memberId)
        .map(id -> id + 1L)
        .orElseThrow(
            () ->
                new PublicDiaryNotFoundException(PublicDiaryErrorSpec.PUBLIC_DIARY_NOT_FOUND, 0L));
  }

  private void replaceWithPreSignedUrls(List<MySharedDiaryPreviewProjection> previews) {
    for (MySharedDiaryPreviewProjection preview : previews) {
      preview.replaceWebtoonImageUrl(
          s3FileStorageService.preSignedFirstWebtoonUrlFrom(preview.getWebtoonImageUrl()));
    }
  }
}
