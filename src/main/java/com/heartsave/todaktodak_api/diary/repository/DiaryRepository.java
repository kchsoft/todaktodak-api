package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIdsProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryYearMonthProjection;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

  boolean existsByMemberEntity_IdAndDiaryCreatedTimeBetween(
      Long memberId, Instant start, Instant end);

  @Modifying
  @Query(
      value = "DELETE FROM DiaryEntity d WHERE :diaryId = d.id and :memberId = d.memberEntity.id")
  int deleteByIds(@Param("memberId") Long memberId, @Param("diaryId") Long diaryId);

  @Query(
      value =
          """
            SELECT d.id as id, d.diaryCreatedTime as diaryCreatedTime
            FROM DiaryEntity d
            WHERE d.memberEntity.id = :memberId AND d.diaryCreatedTime BETWEEN :startTime AND :endTime
            ORDER BY d.diaryCreatedTime ASC
""")
  Optional<List<DiaryYearMonthProjection>> findYearMonthsByMemberIdAndInstants(
      @Param("memberId") Long memberId,
      @Param("startTime") Instant startTime,
      @Param("endTime") Instant endTime);

  @Query(
      value =
          " SELECT d FROM DiaryEntity  d WHERE d.memberEntity.id = :memberId AND CAST (d.diaryCreatedTime as DATE) = :diaryDate ")
  Optional<DiaryEntity> findByMemberIdAndDate(
      @Param("memberId") Long memberId, @Param("diaryDate") LocalDate diaryDate);

  @Query(
      value =
          """
            SELECT d.id as diaryId, d.publicDiaryEntity.id as publicDiaryId
            FROM DiaryEntity d
            LEFT JOIN d.publicDiaryEntity pd
            WHERE d.id = :diaryId
          """)
  Optional<DiaryIdsProjection> findIdsById(@Param("diaryId") Long diaryId);
}
