package com.heartsave.todaktodak_api.domain.diary.service;

import com.heartsave.todaktodak_api.domain.diary.cache.ContentReactionCountCache;
import com.heartsave.todaktodak_api.domain.diary.cache.entity.ContentReactionCountEntity;
import com.heartsave.todaktodak_api.domain.diary.domain.DiaryPageIndex;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class PublicDiaryCacheService {
  private final ContentReactionCountCache contentReactionCountCache;
  private final String ORDER_PIVOT_ENTITY_FORMAT = "%d:%019d";
  private final String ORDER_PIVOT_GET_FORMAT = "({\"orderPivot\":\"%d:%019d\"";

  public void saveContentReactionCounts(List<ContentReactionCountEntity> contents) {
    setOrderPivotPadding(contents);
    contentReactionCountCache.save(contents);
  }

  private void setOrderPivotPadding(List<ContentReactionCountEntity> contents) {
    contents.forEach(
        content ->
            content.setOrderPivot(
                String.format(
                    ORDER_PIVOT_ENTITY_FORMAT,
                    content.getMilsTimeStamp(),
                    content.getPublicDiaryId())));
  }

  public List<ContentReactionCountEntity> getContentReactionCounts(DiaryPageIndex pageIndex) {
    return contentReactionCountCache.get(
        String.format(
            ORDER_PIVOT_GET_FORMAT, pageIndex.getMilsTimeStamp(), pageIndex.getPublicDiaryId()));
  }
}
