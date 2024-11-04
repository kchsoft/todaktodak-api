package com.heartsave.todaktodak_api.diary.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.common.BaseTestEntity;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

@Slf4j
@DataJpaTest
class MySharedDiaryRepositoryTest {

  @Autowired private MySharedDiaryRepository mySharedDiaryRepository;
  @Autowired private TestEntityManager tem;

  private MemberEntity member;
  private List<PublicDiaryEntity> publicDiaries;
  private Long TEST_PUBLIC_DIARY_SIZE = 13L;

  @BeforeEach
  void setUp() {
    member = BaseTestEntity.createMemberNoId();
    tem.persist(member);

    publicDiaries = createTestPublicDiaries();
    tem.flush();
    tem.clear();
  }

  private List<PublicDiaryEntity> createTestPublicDiaries() {
    List<PublicDiaryEntity> publicDiaries = new ArrayList<>();
    for (int i = 0; i < TEST_PUBLIC_DIARY_SIZE; i++) {
      DiaryEntity diary = BaseTestEntity.createDiaryNoIdWithMember(member);
      PublicDiaryEntity publicDiary =
          PublicDiaryEntity.builder()
              .diaryEntity(diary)
              .memberEntity(member)
              .publicContent("public-content-" + (i + 1))
              .build();
      publicDiaries.add(publicDiary);
      tem.persist(diary);
      tem.persist(publicDiary);
    }
    return publicDiaries;
  }

  @Test
  @DisplayName("최신 공개 일기 ID를 성공적으로 조회한다")
  void shouldFindLatestPublicDiaryId() {
    Optional<Long> latestId = mySharedDiaryRepository.findLatestId(member.getId());

    assertThat(latestId).as("최신 공개 일기 ID가 존재해야 합니다").isPresent();
    assertThat(latestId.get())
        .as("최신 공개 일기 ID는 마지막으로 생성된 일기의 ID와 일치해야 합니다")
        .isEqualTo(publicDiaries.getLast().getId());
  }

  @Test
  @DisplayName("공개 일기가 없는 경우 빈 Optional을 반환한다")
  void shouldReturnEmptyOptionalWhenNoPublicDiaries() {
    MemberEntity newMember = BaseTestEntity.createMemberNoId();
    tem.persist(newMember);
    tem.flush();
    tem.clear();

    Optional<Long> latestId = mySharedDiaryRepository.findLatestId(newMember.getId());

    assertThat(latestId).as("공개 일기가 없는 경우 빈 Optional을 반환해야 합니다").isEmpty();
  }

  @Test
  @DisplayName("페이지네이션된 공개 일기 미리보기를 성공적으로 조회한다")
  void shouldFindPaginatedPreviews() {
    Long publicDiaryId = publicDiaries.getLast().getId() + 1;
    int fetchSize = 3;
    PageRequest pageRequest = PageRequest.of(0, fetchSize);

    List<MySharedDiaryPreviewProjection> previews =
        mySharedDiaryRepository.findNextPreviews(member.getId(), publicDiaryId, pageRequest);

    assertThat(previews).as("요청한 페이지 크기(%d)만큼의 미리보기가 조회되어야 합니다", fetchSize).hasSize(fetchSize);
    assertThat(previews.get(0).getPublicDiaryId())
        .as("첫 번째 미리보기는 가장 최근 일기여야 합니다")
        .isEqualTo(publicDiaries.get(TEST_PUBLIC_DIARY_SIZE.intValue() - 1).getId());
    assertThat(previews.get(1).getPublicDiaryId())
        .as("두 번째 미리보기는 두 번째로 최근 일기여야 합니다")
        .isEqualTo(publicDiaries.get(TEST_PUBLIC_DIARY_SIZE.intValue() - 2).getId());
    assertThat(previews.get(2).getPublicDiaryId())
        .as("세 번째 미리보기는 세 번째로 최근 일기여야 합니다")
        .isEqualTo(publicDiaries.get(TEST_PUBLIC_DIARY_SIZE.intValue() - 3).getId());
  }

  @Test
  @DisplayName("특정 ID 이전의 공개 일기를 조회한다")
  void shouldFindPreviewsBeforeSpecificId() {
    // given
    int pivot = 10;
    Long middlePublicDiaryId = publicDiaries.get(pivot).getId();
    PageRequest pageRequest = PageRequest.of(0, 4);

    // when
    List<MySharedDiaryPreviewProjection> previews =
        mySharedDiaryRepository.findNextPreviews(member.getId(), middlePublicDiaryId, pageRequest);

    // then
    assertThat(previews).as("지정된 페이지 크기(4)만큼의 미리보기가 조회되어야 합니다").hasSize(4);
    assertThat(previews.get(0).getPublicDiaryId())
        .as("첫 번째 미리보기의 ID가 예상과 다릅니다")
        .isEqualTo(publicDiaries.get(pivot - 1).getId());
    assertThat(previews.get(1).getPublicDiaryId())
        .as("두 번째 미리보기의 ID가 예상과 다릅니다")
        .isEqualTo(publicDiaries.get(pivot - 2).getId());
    assertThat(previews.get(2).getPublicDiaryId())
        .as("세 번째 미리보기의 ID가 예상과 다릅니다")
        .isEqualTo(publicDiaries.get(pivot - 3).getId());
    assertThat(previews.get(3).getPublicDiaryId())
        .as("네 번째 미리보기의 ID가 예상과 다릅니다")
        .isEqualTo(publicDiaries.get(pivot - 4).getId());
  }

  @Test
  @DisplayName("조회할 데이터가 없는 경우 빈 리스트를 반환한다")
  void shouldReturnEmptyListWhenNoMoreData() {
    // given
    Long firstPublicDiaryId = publicDiaries.get(0).getId();
    PageRequest pageRequest = PageRequest.of(0, 5);

    // when
    List<MySharedDiaryPreviewProjection> previews =
        mySharedDiaryRepository.findNextPreviews(member.getId(), firstPublicDiaryId, pageRequest);

    // then
    assertThat(previews).as("더 이상 조회할 데이터가 없는 경우 빈 리스트를 반환해야 합니다").isEmpty();
  }
}
