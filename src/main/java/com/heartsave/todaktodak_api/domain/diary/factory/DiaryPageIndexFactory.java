package com.heartsave.todaktodak_api.domain.diary.factory;

import static com.heartsave.todaktodak_api.common.constant.TodakConstant.DIARY.PAGE_DEFAULT_ID;
import static com.heartsave.todaktodak_api.common.constant.TodakConstant.DIARY.PAGE_DEFAULT_TIME;

import com.heartsave.todaktodak_api.domain.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.domain.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.domain.diary.entity.projection.DiaryPageIndexProjection;
import com.heartsave.todaktodak_api.domain.diary.repository.MySharedDiaryRepository;
import com.heartsave.todaktodak_api.domain.diary.repository.PublicDiaryRepository;
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
      DiaryPageIndexProjection indexProjection =
          mySharedDiaryRepository
              .findFirstByMemberEntity_IdOrderByCreatedTimeDescIdDesc(memberId)
              .orElse(createPageIndexProjection());

      return Optional.of(DiaryPageIndex.fromLatest(indexProjection));
    }

    return Optional.empty();
  }

  private Optional<DiaryPageIndex> getLatestPublicPageIndex(DiaryPageRequest request) {
    Long publicDiaryId = request.publicDiaryId();
    Instant createdTime = request.createdTime();

    if (publicDiaryId == PAGE_DEFAULT_ID || createdTime.equals(PAGE_DEFAULT_TIME)) {
      DiaryPageIndexProjection indexProjection =
          publicDiaryRepository
              .findFirstByOrderByCreatedTimeDescIdDesc()
              .orElse(createPageIndexProjection());
      return Optional.of(DiaryPageIndex.fromLatest(indexProjection));
    }

    return Optional.empty();
  }

  private DiaryPageIndexProjection createPageIndexProjection() {
    return new DiaryPageIndexProjection() {
      @Override
      public Long getPublicDiaryId() {
        return PAGE_DEFAULT_ID;
      }

      @Override
      public Instant getCreatedTime() {
        return PAGE_DEFAULT_TIME;
      }
    };
  }
}
