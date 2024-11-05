package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.diary.entity.DiaryReactionEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryReactionCountProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiaryReactionRepository extends JpaRepository<DiaryReactionEntity, Long> {

  @Query(
      """
    SELECT CASE WHEN EXISTS (
        SELECT 1
        FROM DiaryReactionEntity dr
        WHERE dr.memberEntity.id = :memberId
        AND dr.diaryEntity.id = :diaryId
        AND dr.reactionType = :reactionType
    ) THEN true ELSE false END
""")
  boolean hasReaction(
      @Param("memberId") Long memberId,
      @Param("diaryId") Long diaryId,
      @Param("reactionType") DiaryReactionType reactionType);

  @Query(
      """
        SELECT dr.reactionType
        FROM DiaryReactionEntity dr
        WHERE dr.memberEntity.id = :memberId AND dr.diaryEntity.id = :diaryId
      """)
  List<DiaryReactionType> findMemberReaction(Long memberId, Long diaryId);

  @Query(
      value =
          """
            SELECT
              COUNT(CASE WHEN reaction_type = 'LIKE' THEN 1 END) as likes,
              COUNT(CASE WHEN reaction_type = 'SURPRISED' THEN 1 END) as surprised,
              COUNT(CASE WHEN reaction_type = 'EMPATHIZE' THEN 1 END) as empathize,
              COUNT(CASE WHEN reaction_type = 'CHEERING' THEN 1 END) as cheering
            FROM diary_reaction
            WHERE diary_id = :diaryId
          """,
      nativeQuery = true)
  DiaryReactionCountProjection countEachByDiaryId(Long diaryId);

  @Modifying
  @Query(
      """
        DELETE FROM DiaryReactionEntity  dr WHERE dr.memberEntity.id = :memberId AND dr.diaryEntity.id = :diaryId AND dr.reactionType = :reactionType
      """)
  int deleteReaction(
      @Param("memberId") Long memberId,
      @Param("diaryId") Long diaryId,
      @Param("reactionType") DiaryReactionType reactionType);
}
