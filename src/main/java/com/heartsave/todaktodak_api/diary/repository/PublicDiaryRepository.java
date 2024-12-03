package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryPageIndexProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublicDiaryRepository extends JpaRepository<PublicDiaryEntity, Long> {

  Optional<DiaryPageIndexProjection> findFirstByOrderByCreatedTimeDescIdDesc();

  @Query(
      """
        SELECT new com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentProjection(
            pd.id,
            d.id,
            m.characterImageUrl,
            m.nickname,
            pd.publicContent,
            d.webtoonImageUrl,
            d.bgmUrl,
            d.diaryCreatedTime
        )
        FROM PublicDiaryEntity pd
        JOIN pd.diaryEntity d
        JOIN pd.memberEntity m
        WHERE pd.createdTime <= :#{#index.createdTime} AND pd.id < :#{#index.publicDiaryId}
        ORDER BY pd.createdTime DESC, pd.id DESC
        """)
  List<PublicDiaryContentProjection> findNextContents(
      @Param("index") DiaryPageIndex index, Pageable pageable);
}
