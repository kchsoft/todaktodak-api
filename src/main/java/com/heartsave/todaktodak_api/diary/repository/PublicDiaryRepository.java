package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentOnlyProjection;
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
        SELECT new com.heartsave.todaktodak_api.diary.entity.projection.PublicDiaryContentOnlyProjection(
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
        WHERE pd.id < :publicDiaryId
        ORDER BY pd.createdTime DESC, pd.id DESC
        """)
  List<PublicDiaryContentOnlyProjection> findNextContentOnlyById(
      @Param("publicDiaryId") Long publicDiaryId, Pageable pageable);
}
