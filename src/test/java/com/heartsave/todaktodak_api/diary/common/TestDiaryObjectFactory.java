package com.heartsave.todaktodak_api.diary.common;

import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIndexProjection;
import java.time.LocalDateTime;
import java.util.List;

public class TestDiaryObjectFactory {
  public static List<DiaryIndexProjection> getTestDiaryIndexProjections_2024_03_Data_Of_2() {
    List<DiaryIndexProjection> mockIndexes =
        List.of(
            new DiaryIndexProjection() {
              @Override
              public Long getId() {
                return 1L;
              }

              @Override
              public LocalDateTime getDiaryCreatedTime() {
                return LocalDateTime.of(2024, 3, 15, 14, 30);
              }
            },
            new DiaryIndexProjection() {
              @Override
              public Long getId() {
                return 2L;
              }

              @Override
              public LocalDateTime getDiaryCreatedTime() {
                return LocalDateTime.of(2024, 3, 20, 16, 45);
              }
            });
    return mockIndexes;
  }
}
