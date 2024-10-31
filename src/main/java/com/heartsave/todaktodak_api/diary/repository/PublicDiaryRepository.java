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
        SELECT new com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryViewProjection(
            pd.id,
            d.id,
            m.characterImageUrl,
            m.nickname,
            pd.publicContent,
            d.webtoonImageUrl,
            d.bgmUrl,
            CAST(d.diaryCreatedTime as LocalDate)
        )
        FROM PublicDiaryEntity pd
        JOIN pd.diaryEntity d
        JOIN pd.memberEntity m
        WHERE pd.id < :publicDiaryId
        ORDER BY pd.id DESC
        """)
  List<PublicDiaryViewProjection> findViewsById(
      @Param("publicDiaryId") Long publicDiaryId, Pageable pageable);
}
