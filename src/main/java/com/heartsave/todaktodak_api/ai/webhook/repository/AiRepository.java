package com.heartsave.todaktodak_api.ai.webhook.repository;

import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AiRepository extends JpaRepository<DiaryEntity, Long> {
  @Modifying
  @Query(
      value =
          """
        UPDATE DiaryEntity d
        SET d.webtoonImageUrl = :url
        WHERE d.memberEntity.id = :memberId AND  CAST(d.diaryCreatedTime AS DATE ) = :createdDate
""")
  void updateWebtoonUrl(
      @Param("memberId") Long memberId,
      @Param("createdDate") LocalDate createdDate,
      @Param("url") String url);

  @Query(
      value =
          """
        SELECT
        CASE WHEN d.bgmUrl = "" THEN TRUE
        ELSE FALSE END
        FROM DiaryEntity d
        WHERE d.memberEntity.id = :memberId AND CAST(d.diaryCreatedTime AS DATE) = :createdDate
""")
  Boolean isDefaultBgmUrl(
      @Param("memberId") Long memberId, @Param("createdDate") LocalDate createdDate);
}
