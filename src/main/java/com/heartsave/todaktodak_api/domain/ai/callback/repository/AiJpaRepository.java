package com.heartsave.todaktodak_api.domain.ai.callback.repository;

import com.heartsave.todaktodak_api.domain.diary.entity.DiaryEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiJpaRepository extends JpaRepository<DiaryEntity, Long> {
  @Modifying
  @Query(
      value =
          """
        UPDATE DiaryEntity d
        SET d.webtoonImageUrl = :url
        WHERE d.memberEntity.id = :memberId AND  d.diaryCreatedTime = :createdDate
""")
  int updateWebtoonUrl(
      @Param("memberId") Long memberId,
      @Param("createdDate") Instant createdDate,
      @Param("url") String url);

  @Modifying
  @Query(
      value =
          """
                UPDATE DiaryEntity d
                SET d.bgmUrl = :url
                WHERE d.memberEntity.id = :memberId AND d.diaryCreatedTime = :createdDate
        """)
  int updateBgmUrl(
      @Param("memberId") Long memberId,
      @Param("createdDate") Instant createdDate,
      @Param("url") String url);

  @Query(
      value =
          """
        SELECT
        CASE WHEN d.bgmUrl != "" AND d.webtoonImageUrl != "" THEN TRUE
        ELSE FALSE END
        FROM DiaryEntity d
        WHERE d.memberEntity.id = :memberId AND d.diaryCreatedTime  = :createdDate
""")
  Optional<Boolean> isContentCompleted(
      @Param("memberId") Long memberId, @Param("createdDate") Instant createdDate);
}
