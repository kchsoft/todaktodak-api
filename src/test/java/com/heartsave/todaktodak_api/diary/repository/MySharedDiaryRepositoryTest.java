package com.heartsave.todaktodak_api.diary.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.converter.InstantUtils;
import com.heartsave.todaktodak_api.diary.domain.DiaryPageIndex;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryPageRequest;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.PublicDiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryPageIndexProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryContentProjection;
import com.heartsave.todaktodak_api.diary.entity.projection.MySharedDiaryPreviewProjection;
import com.heartsave.todaktodak_api.diary.factory.DiaryPageIndexFactory;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@DataJpaTest(
    includeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = DiaryPageIndexFactory.class))
class MySharedDiaryRepositoryTest {

  @Autowired private MySharedDiaryRepository mySharedDiaryRepository;
  @Autowired private DiaryPageIndexFactory indexFactory;
  @Autowired private TestEntityManager tem;

  private MemberEntity member;
  private List<PublicDiaryEntity> publicDiaries;
  private Long TEST_PUBLIC_DIARY_SIZE = 13L;

  @BeforeEach
  void setUp() {
    member = BaseTestObject.createMemberNoId();
    tem.persist(member);

    publicDiaries = createTestPublicDiaries();
    tem.flush();
    tem.clear();
  }

  private List<PublicDiaryEntity> createTestPublicDiaries() {
    for (int i = 0; i < TEST_PUBLIC_DIARY_SIZE; i++) {
      DiaryEntity diary =
          BaseTestObject.createDiaryNoIdWithMemberAndCreatedDateTime(
              member, Instant.now().minus(TEST_PUBLIC_DIARY_SIZE - (i + 1), ChronoUnit.DAYS));
      PublicDiaryEntity publicDiary =
          PublicDiaryEntity.builder()
              .diaryEntity(diary)
              .memberEntity(member)
              .publicContent("public-content-" + (i + 1))
              .build();
      tem.persist(diary);
      tem.persist(publicDiary);
    }
    tem.flush();
    tem.clear();
    return new ArrayList<>(mySharedDiaryRepository.findAll());
  }

  @Test
  @DisplayName("최신 공개 일기 ID를 성공적으로 조회한다")
  void shouldFindLatestPublicDiaryId() {
    Optional<DiaryPageIndexProjection> indexProjection =
        mySharedDiaryRepository.findFirstByMemberEntity_IdOrderByCreatedTimeDescIdDesc(
            member.getId());
    assertThat(indexProjection).as("최신 공개 일기 ID가 존재해야 합니다").isPresent();
    assertThat(indexProjection.get().getPublicDiaryId())
        .as("최신 공개 일기 ID는 마지막으로 생성된 일기의 ID와 일치해야 합니다")
        .isEqualTo(publicDiaries.getLast().getId());
  }

  @Test
  @DisplayName("공개 일기가 없는 경우 빈 Optional을 반환한다")
  void shouldReturnEmptyOptionalWhenNoPublicDiaries() {
    MemberEntity newMember = BaseTestObject.createMemberNoId();
    tem.persist(newMember);
    tem.flush();
    tem.clear();

    Optional<DiaryPageIndexProjection> indexProjection =
        mySharedDiaryRepository.findFirstByMemberEntity_IdOrderByCreatedTimeDescIdDesc(
            newMember.getId());
    assertThat(indexProjection).as("공개 일기가 없는 경우 빈 Optional을 반환해야 합니다").isEmpty();
    System.out.println("indexProjection = " + indexProjection);
  }

  @Test
  @DisplayName("페이지네이션된 공개 일기 미리보기를 성공적으로 조회한다")
  void shouldFindPaginatedPreviews() {
    int fetchSize = 3;
    PageRequest pageRequest = PageRequest.of(0, fetchSize);
    DiaryPageRequest request =
        new DiaryPageRequest(
            publicDiaries.getLast().getId(), publicDiaries.getLast().getCreatedTime());
    DiaryPageIndex pageIndex = indexFactory.createFrom(request, member.getId());

    List<MySharedDiaryPreviewProjection> previews =
        mySharedDiaryRepository.findNextPreviews(member.getId(), pageIndex, pageRequest);

    assertThat(previews).as("요청한 페이지 크기(%d)만큼의 미리보기가 조회되어야 합니다", fetchSize).hasSize(fetchSize);
    assertThat(previews.get(0).getPublicDiaryId())
        .as("첫 번째 미리보기는 가장 최근 일기여야 합니다")
        .isEqualTo(
            publicDiaries
                .get(TEST_PUBLIC_DIARY_SIZE.intValue() - 2)
                .getId()); // max size == 12, index id 13,
    assertThat(previews.get(1).getPublicDiaryId())
        .as("두 번째 미리보기는 두 번째로 최근 일기여야 합니다")
        .isEqualTo(publicDiaries.get(TEST_PUBLIC_DIARY_SIZE.intValue() - 3).getId());
    assertThat(previews.get(2).getPublicDiaryId())
        .as("세 번째 미리보기는 세 번째로 최근 일기여야 합니다")
        .isEqualTo(publicDiaries.get(TEST_PUBLIC_DIARY_SIZE.intValue() - 4).getId());
  }

  @Test
  @DisplayName("특정 ID 이전의 공개 일기를 조회한다")
  void shouldFindPreviewsBeforeSpecificId() {
    // given
    int pivot = 10;
    Long middlePublicDiaryId = publicDiaries.get(pivot).getId(); // 11
    Instant middleCreatedTime = publicDiaries.get(pivot).getCreatedTime();
    DiaryPageRequest request = new DiaryPageRequest(middlePublicDiaryId, middleCreatedTime);
    DiaryPageIndex pageIndex = indexFactory.createFrom(request);
    PageRequest pageRequest = PageRequest.of(0, 4);
    // when
    List<MySharedDiaryPreviewProjection> previews =
        mySharedDiaryRepository.findNextPreviews(member.getId(), pageIndex, pageRequest);

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
    Instant firstCreatedTime = publicDiaries.get(0).getCreatedTime();
    DiaryPageRequest request = new DiaryPageRequest(firstPublicDiaryId, firstCreatedTime);
    DiaryPageIndex pageIndex = indexFactory.createFrom(request);
    PageRequest pageRequest = PageRequest.of(0, 5);

    // when
    List<MySharedDiaryPreviewProjection> previews =
        mySharedDiaryRepository.findNextPreviews(member.getId(), pageIndex, pageRequest);

    // then
    assertThat(previews).as("더 이상 조회할 데이터가 없는 경우 빈 리스트를 반환해야 합니다").isEmpty();
  }

  @Test
  @DisplayName("날짜로 공유된 일기 내용을 성공적으로 조회한다")
  void findContentOnly_Success() {
    publicDiaries = mySharedDiaryRepository.findAll();

    PublicDiaryEntity expected = publicDiaries.get(3);
    Long memberId = expected.getMemberEntity().getId();
    DiaryEntity diaryEntity = expected.getDiaryEntity();
    LocalDate requestDate =
        InstantUtils.toLocalDate(expected.getDiaryEntity().getDiaryCreatedTime());

    Optional<MySharedDiaryContentProjection> Optional_actual =
        mySharedDiaryRepository.findContent(memberId, requestDate);

    assertThat(Optional_actual).as("조회된 결과가 존재해야 합니다").isPresent();

    MySharedDiaryContentProjection actual = Optional_actual.get();
    assertThat(actual.getPublicDiaryId()).as("공개된 일기 ID가 일치해야 합니다").isEqualTo(expected.getId());
    assertThat(actual.getDiaryId())
        .as("원본 일기 ID가 일치해야 합니다")
        .isEqualTo(expected.getDiaryEntity().getId());
    assertThat(actual.getPublicContent())
        .as("공개된 일기 내용이 일치해야 합니다")
        .isEqualTo(expected.getPublicContent());
    assertThat(actual.getWebtoonImageUrls())
        .as("웹툰 이미지 URL이 일치해야 합니다")
        .contains(expected.getDiaryEntity().getWebtoonImageUrl());
    assertThat(actual.getBgmUrl())
        .as("BGM URL이 일치해야 합니다")
        .contains(expected.getDiaryEntity().getBgmUrl());
    assertThat(actual.getDiaryCreatedDate().truncatedTo(ChronoUnit.SECONDS))
        .as("일기 작성 날짜가 일치해야 합니다")
        .isEqualTo(
            expected
                .getDiaryEntity()
                .getDiaryCreatedTime()
                .truncatedTo(ChronoUnit.SECONDS)); // DB에 Instant 저장시 MILLIS 반올림, 따라서 Second 까지 검사
  }

  @Test
  @DisplayName("존재하지 않는 날짜의 공유된 일기를 조회하면 빈 Optional을 반환한다")
  void findContentOnly_ReturnEmpty_WhenNotFound() {
    LocalDate nonExistentDate = LocalDate.of(3024, 1, 1);
    Long memberId = member.getId();

    Optional<MySharedDiaryContentProjection> result =
        mySharedDiaryRepository.findContent(memberId, nonExistentDate);
    assertThat(result).as("존재하지 않는 날짜로 일기 조회 시 빈 Optional이 반환되어야 합니다").isEmpty();

    result = mySharedDiaryRepository.findContent(1000L, LocalDate.now());
    assertThat(result).as("존재하지 않는 memberId로 일기 조회 시 빈 Optional이 반환되어야 합니다").isEmpty();
  }

  @TestConfiguration
  @EnableJpaAuditing
  public static class TestAuditConfig {}
}
