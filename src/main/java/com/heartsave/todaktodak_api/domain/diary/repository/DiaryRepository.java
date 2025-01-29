package com.heartsave.todaktodak_api.domain.diary.repository;

import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.domain.diary.entity.projection.DiaryYearMonthProjection;
import java.time.Instant;
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

  List<DiaryYearMonthProjection>
      findByMemberEntity_IdAndDiaryCreatedTimeBetweenOrderByDiaryCreatedTimeDesc(
          Long memberId, Instant startTime, Instant endTime);

  Optional<DiaryEntity> findDiaryEntityByMemberEntity_IdAndDiaryCreatedTimeBetween(
      Long memberId, Instant startTime, Instant endTime);

  Optional<DiaryEntity> findDiaryEntityByMemberEntity_IdAndId(Long memberId, Long diaryId);
}
