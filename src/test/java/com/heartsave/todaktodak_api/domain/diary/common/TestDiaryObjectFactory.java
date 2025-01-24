package com.heartsave.todaktodak_api.domain.diary.common;

import com.heartsave.todaktodak_api.domain.diary.entity.projection.DiaryYearMonthProjection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class TestDiaryObjectFactory {
  public static List<DiaryYearMonthProjection> getTestDiaryIndexProjections_2024_03_Data_Of_2() {
    List<DiaryYearMonthProjection> mockIndexes =
        List.of(
            new DiaryYearMonthProjection() {
              @Override
              public Long getId() {
                return 1L;
              }

              @Override
              public Instant getDiaryCreatedTime() {
                return LocalDateTime.of(2024, 3, 15, 14, 30).atZone(ZoneId.of("UTC")).toInstant();
              }
            },
            new DiaryYearMonthProjection() {
              @Override
              public Long getId() {
                return 2L;
              }

              @Override
              public Instant getDiaryCreatedTime() {
                return LocalDateTime.of(2024, 3, 20, 16, 45).atZone(ZoneId.of("UTC")).toInstant();
              }
            });
    return mockIndexes;
  }
}
