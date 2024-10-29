package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.entity.DiaryReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryReactionRepository extends JpaRepository<DiaryReactionEntity, Long> {

  @Modifying
  @Query(
      """
        DELETE FROM DiaryReactionEntity  dr WHERE dr.memberEntity.id = :memberId AND dr.diaryEntity.id = :diaryId AND dr.reactionType = :reactionType
    """)
  int deleteByMemberIdAndDiaryIdAndReactionType(
      @Param("memberId") Long memberId,
      @Param("diaryId") Long diaryId,
      @Param("reactionType") DiaryReactionType reactionType);
}
