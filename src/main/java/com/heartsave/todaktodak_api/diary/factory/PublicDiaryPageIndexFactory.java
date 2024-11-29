package com.heartsave.todaktodak_api.diary.factory;

import static com.heartsave.todaktodak_api.common.constant.CoreConstant.DIARY.PAGE_DEFAULT_ID;
import static com.heartsave.todaktodak_api.common.constant.CoreConstant.DIARY.PAGE_DEFAULT_TIME;

import com.heartsave.todaktodak_api.diary.domain.PublicDiaryPageIndex;
import com.heartsave.todaktodak_api.diary.dto.request.PublicDiaryPageRequest;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryPageIndexProjection;
import com.heartsave.todaktodak_api.diary.repository.PublicDiaryRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class PublicDiaryPageIndexFactory {
  private final PublicDiaryRepository repository;

  @Transactional(readOnly = true)
  public PublicDiaryPageIndex createFrom(PublicDiaryPageRequest request) {
    return getLatestPageIndex(request).orElse(PublicDiaryPageIndex.from(request));
  }

  private Optional<PublicDiaryPageIndex> getLatestPageIndex(PublicDiaryPageRequest request) {
    Long publicDiaryId = request.publicDiaryId();
    Instant createdTime = request.createdTime();

    if (publicDiaryId == PAGE_DEFAULT_ID || createdTime.equals(PAGE_DEFAULT_TIME)) {
      PublicDiaryPageIndexProjection indexProjection =
          repository
              .findLatestIdAndCreatedTime()
              .orElse(
                  PublicDiaryPageIndexProjection.builder()
                      .publicDiaryId(PAGE_DEFAULT_ID)
                      .createdTime(PAGE_DEFAULT_TIME)
                      .build());
      return Optional.of(PublicDiaryPageIndex.fromLatest(indexProjection));
    }

    return Optional.empty();
  }
}
