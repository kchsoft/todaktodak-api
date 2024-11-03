package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MySharedDiaryRepository extends JpaRepository<PublicDiaryEntity, Long> {

  @Query(
      value =
          """
            SELECT MAX(pd.id)
            FROM PublicDiaryEntity pd
""")
  Optional<Long> findLatestId(Long memberId);

  @Query(
      value =
          """
                      SELECT pd.id, d.webtoonImageUrl, pd.createdTime
                      FROM PublicDiaryEntity pd JOIN DiaryEntity d ON pd.diaryEntity.id = d.id
                      WHERE pd.memberEntity.id = :mamberId AND pd.id < :publicDiaryId
                      ORDER BY pd.id DESC
          """)
  List<MySharedDiaryPreviewProjection> findNextPreviews(
      @Param("memberId") Long memberId,
      @Param("publicDiaryId") Long publicDiaryId,
      Pageable pageable);
}
