package com.heartsave.todaktodak_api.diary.repository;

import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryContentOnlyProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import java.time.LocalDate;
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
            WHERE pd.memberEntity.id = :memberId
""")
  Optional<Long> findLatestId(@Param("memberId") Long memberId);

  @Query(
      value =
          """
                      SELECT
                       new com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection(
                       pd.id,
                       d.webtoonImageUrl,
                       CAST (pd.createdTime AS Localdate)
                       )
                      FROM PublicDiaryEntity pd JOIN pd.diaryEntity d
                      WHERE pd.memberEntity.id = :memberId AND pd.id < :publicDiaryId
                      ORDER BY pd.id DESC
          """)
  List<MySharedDiaryPreviewProjection> findNextPreviews(
      @Param("memberId") Long memberId,
      @Param("publicDiaryId") Long publicDiaryId,
      Pageable pageable);

  @Query(
      """
            SELECT new com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryContentOnlyProjection(
                pd.id,
                d.id,
                pd.publicContent,
                d.webtoonImageUrl,
                d.bgmUrl,
                CAST(d.diaryCreatedTime as LocalDate)
            )
            FROM PublicDiaryEntity pd
            JOIN pd.diaryEntity d
            WHERE  pd.memberEntity.id = :memberId AND CAST(pd.createdTime AS LocalDate) = :publicDiaryDate
            """)
  Optional<MySharedDiaryContentOnlyProjection> findContentOnly(
      @Param("memberId") Long memberId, @Param("publicDiaryDate") LocalDate publicDiaryDate);
}
