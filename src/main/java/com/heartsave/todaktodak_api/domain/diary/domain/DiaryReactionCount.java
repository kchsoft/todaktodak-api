package com.heartsave.todaktodak_api.domain.diary.domain;

import com.heartsave.todaktodak_api.domain.diary.entity.projection.DiaryReactionCountProjection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DiaryReactionCount {
  private Long like;
  private Long surprised;
  private Long empathize;
  private Long cheering;

  private DiaryReactionCount(DiaryReactionCountProjection projection) {
    this(
        projection.getLikes(),
        projection.getSurprised(),
        projection.getEmpathize(),
        projection.getCheering());
  }

  public static Map<Long, DiaryReactionCount> from(List<DiaryReactionCountProjection> projections) {
    Map<Long, DiaryReactionCount> result = new ConcurrentHashMap<>();
    for (DiaryReactionCountProjection pro : projections) {
      result.put(pro.getPublicDiaryId(), new DiaryReactionCount(pro));
    }
    return result;
  }
}
