package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIdsProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIndexProjection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

  @Query(
      value =
          "SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DiaryEntity d "
              + "WHERE d.memberEntity.id = :memberId "
              + "AND CAST(d.diaryCreatedTime AS date) = CAST(:diaryDate AS date)")
  boolean existsByDate(
      @Param("memberId") Long memberId, @Param("diaryDate") LocalDateTime diaryDate);

  @Modifying
  @Query(
      value = "DELETE FROM DiaryEntity d WHERE :diaryId = d.id and :memberId = d.memberEntity.id")
  int deleteByIds(@Param("memberId") Long memberId, @Param("diaryId") Long diaryId);

  @Query(
      value =
          "SELECT d.id as id, d.diaryCreatedTime as diaryCreatedTime FROM DiaryEntity d WHERE d.memberEntity.id = :memberId AND d.diaryCreatedTime BETWEEN :startDateTime AND :endDateTime ORDER BY d.diaryCreatedTime ASC")
  Optional<List<DiaryIndexProjection>> findIndexesByMemberIdAndDateTimes(
      @Param("memberId") Long memberId,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime);

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
