package com.heartsave.todaktodak_api.diary.service;

import com.heartsave.todaktodak_api.diary.cache.DiaryReactionCache;
import com.heartsave.todaktodak_api.diary.cache.PublicDiaryCache;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.domain.DiaryReactionCount;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection;
import io.jsonwebtoken.lang.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class PublicDiaryCacheService {
  private final String PUBLIC_DIARY_KEY = "public:diary:cache";
  private final PublicDiaryCache publicDiaryCache;
  private final DiaryReactionCache diaryReactionCache;

  public void saveContents(DiaryPageIndex pageIndex, List<PublicDiaryContentProjection> contents) {
    publicDiaryCache.save(PUBLIC_DIARY_KEY, pageIndex, contents);
  }

  public List<PublicDiaryContentProjection> getContents(DiaryPageIndex pageIndex) {
    return publicDiaryCache.get(PUBLIC_DIARY_KEY, pageIndex).orElse(Collections.emptyList());
  }

  public void saveReactionCount(Long publicDiaryId, DiaryReactionCount count) {
    diaryReactionCache.saveCount(PUBLIC_DIARY_KEY, publicDiaryId, count);
  }

  public Optional<DiaryReactionCount> getReactionCount(Long publicDiaryId) {
    return diaryReactionCache.getCount(PUBLIC_DIARY_KEY, publicDiaryId);
  }
}
