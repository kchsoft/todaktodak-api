package com.heartsave.todaktodak_api.domain.diary.domain;

import com.heartsave.todaktodak_api.domain.diary.entity.projection.DiaryReactionCountProjection;
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

  public static DiaryReactionCount from(DiaryReactionCountProjection projection) {
    return new DiaryReactionCount(
        projection.getLikes(),
        projection.getSurprised(),
        projection.getEmpathize(),
        projection.getCheering());
  }
}
