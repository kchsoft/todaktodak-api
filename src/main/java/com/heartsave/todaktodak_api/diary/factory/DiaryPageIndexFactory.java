package com.heartsave.todaktodak_api.diary.factory;

import static com.heartsave.todaktodak_api.common.constant.CoreConstant.DIARY.PAGE_DEFAULT_ID;
import static com.heartsave.todaktodak_api.common.constant.CoreConstant.DIARY.PAGE_DEFAULT_TIME;

import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryPageIndexProjection;
import com.heartsave.todaktodak_api.diary.repository.MySharedDiaryRepository;
import com.heartsave.todaktodak_api.diary.repository.PublicDiaryRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class DiaryPageIndexFactory {
  private final PublicDiaryRepository publicDiaryRepository;
  private final MySharedDiaryRepository mySharedDiaryRepository;

  public DiaryPageIndex createFrom(DiaryPageRequest request) {
    return getLatestPublicPageIndex(request).orElse(DiaryPageIndex.from(request));
  }

  public DiaryPageIndex createFrom(DiaryPageRequest request, Long memberId) {
    return getLatestMyPageIndex(memberId, request).orElse(DiaryPageIndex.from(request));
  }

  private Optional<DiaryPageIndex> getLatestMyPageIndex(Long memberId, DiaryPageRequest request) {
    Long publicDiaryId = request.publicDiaryId();
    Instant createdTime = request.createdTime();

    if (publicDiaryId == PAGE_DEFAULT_ID || createdTime.equals(PAGE_DEFAULT_TIME)) {
      PublicDiaryPageIndexProjection indexProjection =
          mySharedDiaryRepository
              .findLatestCreatedTimeAndId(memberId)
              .orElse(
                  PublicDiaryPageIndexProjection.builder()
                      .publicDiaryId(PAGE_DEFAULT_ID)
                      .createdTime(PAGE_DEFAULT_TIME)
                      .build());
      return Optional.of(DiaryPageIndex.fromLatest(indexProjection));
    }

    return Optional.empty();
  }

  private Optional<DiaryPageIndex> getLatestPublicPageIndex(DiaryPageRequest request) {
    Long publicDiaryId = request.publicDiaryId();
    Instant createdTime = request.createdTime();

    if (publicDiaryId == PAGE_DEFAULT_ID || createdTime.equals(PAGE_DEFAULT_TIME)) {
      PublicDiaryPageIndexProjection indexProjection =
          publicDiaryRepository
              .findLatestCreatedTimeAndId()
              .orElse(
                  PublicDiaryPageIndexProjection.builder()
                      .publicDiaryId(PAGE_DEFAULT_ID)
                      .createdTime(PAGE_DEFAULT_TIME)
                      .build());
      return Optional.of(DiaryPageIndex.fromLatest(indexProjection));
    }

    return Optional.empty();
  }
}
