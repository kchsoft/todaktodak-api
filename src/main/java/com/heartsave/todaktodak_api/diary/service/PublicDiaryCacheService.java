package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.diary.cache.ContentReactionCountCache;
import com.heartsave.todaktodak_api.diary.cache.entity.ContentReactionCountEntity;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class PublicDiaryCacheService {
  private final ContentReactionCountCache contentReactionCountCache;
  private final String ORDER_PIVOT_FORMAT = "%d:%19d";

  public void saveContentReactionCounts(
      DiaryPageIndex pageIndex, List<ContentReactionCountEntity> contents) {
    setOrderPivotPadding(pageIndex, contents);
    contentReactionCountCache.save(contents);
  }

  private void setOrderPivotPadding(
      DiaryPageIndex pageIndex, List<ContentReactionCountEntity> contents) {
    contents.forEach(content -> content.setOrderPivot(joinOrderPivot(pageIndex)));
  }

  private String joinOrderPivot(DiaryPageIndex pageIndex) {
    return String.format(
        ORDER_PIVOT_FORMAT, pageIndex.getMilsTimeStamp(), pageIndex.getPublicDiaryId());
  }

  public List<ContentReactionCountEntity> getContentReactionCounts(DiaryPageIndex pageIndex) {
    return contentReactionCountCache.get(joinOrderPivot(pageIndex));
  }
}
