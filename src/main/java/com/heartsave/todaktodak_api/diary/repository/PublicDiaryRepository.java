package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryViewProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublicDiaryRepository extends JpaRepository<PublicDiaryEntity, Long> {

  @Query("SELECT MAX(pd.id) FROM PublicDiaryEntity pd")
  Optional<Long> findLatestId();

  @Query(
      """
            SELECT
                pd.id as publicDiaryId,
                d.id as diaryId,
                m.characterImageUrl as characterImageUrl,
                m.nickname as nickname,
                pd.publicContent as publicContent,
                d.webtoonImageUrl as webtoonImageUrl,
                d.bgmUrl as bgmUrl,
                CAST(d.diaryCreatedTime as LocalDate) as date
            FROM PublicDiaryEntity pd
            JOIN FETCH pd.diaryEntity d
            JOIN FETCH pd.memberEntity m
            WHERE pd.id < :publicDiaryId
            ORDER BY pd.id DESC
            """)
  List<PublicDiaryViewProjection> findViewsById(
      @Param("publicDiaryId") Long publicDiaryId, Pageable pageable);
}
