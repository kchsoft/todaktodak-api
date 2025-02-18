package com.heartsave.todaktodak_api.domain.diary.repository;

import com.heartsave.todaktodak_api.domain.diary.constant.DiaryReactionType;
import com.heartsave.todaktodak_api.domain.diary.entity.DiaryReactionEntity;
import com.heartsave.todaktodak_api.domain.diary.entity.projection.DiaryReactionCountProjection;
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
        AND dr.publicDiaryEntity.id = :publicDiaryId
        AND dr.reactionType = :reactionType
    ) THEN true ELSE false END
""")
  boolean hasReaction(
      @Param("memberId") Long memberId,
      @Param("publicDiaryId") Long publicDiaryId,
      @Param("reactionType") DiaryReactionType reactionType);

  @Query(
      """
        SELECT dr.reactionType
        FROM DiaryReactionEntity dr
        WHERE dr.memberEntity.id = :memberId AND dr.publicDiaryEntity.id = :publicDiaryId
      """)
  List<DiaryReactionType> findMemberReactions(Long memberId, Long publicDiaryId);

  @Query(
      value =
          """
              SELECT
                COALESCE(SUM(CASE WHEN dr.reactionType = 'LIKE' THEN 1 END),0) as likes,
                COALESCE(SUM(CASE WHEN dr.reactionType = 'SURPRISED' THEN 1 END),0) as surprised,
                COALESCE(SUM(CASE WHEN dr.reactionType = 'EMPATHIZE' THEN 1 END),0) as empathize,
                COALESCE(SUM(CASE WHEN dr.reactionType = 'CHEERING' THEN 1 END),0) as cheering
              FROM DiaryReactionEntity dr
              WHERE dr.publicDiaryEntity.id = :publicDiaryId
            """)
  DiaryReactionCountProjection countEachByPublicDiaryId(Long publicDiaryId);

  @Query(
      value =
          """
                    SELECT
                      dr.publicDiaryEntity.id as publicDiaryId,
                      SUM(CASE WHEN dr.reactionType = 'LIKE' THEN 1 ELSE 0 END) as likes,
                      SUM(CASE WHEN dr.reactionType = 'SURPRISED' THEN 1 ELSE 0 END) as surprised,
                      SUM(CASE WHEN dr.reactionType = 'EMPATHIZE' THEN 1 ELSE 0 END) as empathize,
                      SUM(CASE WHEN dr.reactionType = 'CHEERING' THEN 1 ELSE 0 END) as cheering
                    FROM DiaryReactionEntity dr
                    WHERE dr.publicDiaryEntity.id IN :publicDiaryIds
                    GROUP BY dr.publicDiaryEntity.id
                          """)
  List<DiaryReactionCountProjection> countEachByPublicDiaryIds(List<Long> publicDiaryIds);

  @Modifying
  @Query(
      """
        DELETE FROM DiaryReactionEntity dr
        WHERE dr.memberEntity.id = :memberId
          AND dr.publicDiaryEntity.id = :publicDiaryId
          AND dr.reactionType = :reactionType
      """)
  int deleteReaction(
      @Param("memberId") Long memberId,
      @Param("publicDiaryId") Long publicDiaryId,
      @Param("reactionType") DiaryReactionType reactionType);
}
