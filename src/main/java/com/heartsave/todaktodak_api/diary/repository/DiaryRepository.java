package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryRepository extends JpaRepository<DiaryEntity, Long> {

  @Query(
      value =
          "SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DiaryEntity d "
              + "WHERE d.memberEntity.id = :memberId "
              + "AND CAST(d.diaryCreatedAt AS date) = CAST(:diaryDate AS date)")
  boolean existsByDate(
      @Param("memberId") Long memberId, @Param("diaryDate") LocalDateTime diaryDate);

  @Modifying
  @Query(
      value = "DELETE FROM DiaryEntity d WHERE :diaryId = d.id and :memberId = d.memberEntity.id")
  int deleteByIds(@Param("memberId") Long memberId, @Param("diaryId") Long diaryId);
}
