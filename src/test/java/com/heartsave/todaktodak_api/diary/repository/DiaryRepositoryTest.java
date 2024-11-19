package com.heartsave.todaktodak_api.diary.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.converter.InstantConverter;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIndexProjection;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@DataJpaTest
public class DiaryRepositoryTest {

  @Autowired private DiaryRepository diaryRepository;
  @Autowired private MemberRepository memberRepository;

  private MemberEntity member;
  private DiaryEntity diary;

  @BeforeEach
  void setup() {
    member = BaseTestObject.createMemberNoId();
    diary = BaseTestObject.createDiaryNoIdWithMember(member);
    memberRepository.save(member);
    diaryRepository.save(diary);
  }

  @Test
  @DisplayName("특정 날짜에 해당되는 사용자 일기 없음.")
  void notExistDiaryByDateAndMember() {
    LocalDate testTime = LocalDate.of(2025, 10, 2);
    boolean exist = diaryRepository.existsByDate(member.getId(), testTime);
    assertThat(exist).as("memberID와 날짜에 해당하는 일기가 있습니다.").isFalse();
  }

  @Test
  @DisplayName("특정 날짜에 해당되는 사용자 일기가 있음.")
  void existDiaryByDateAndMember() {
    boolean exist =
        diaryRepository.existsByDate(
            member.getId(), InstantConverter.toLocalDate(diary.getDiaryCreatedTime()));
    assertThat(exist).as("memberID와 날짜에 해당하는 일기가 없습니다.").isTrue();
  }

  @Test
  @DisplayName(" 요청한 멤버 ID 및 일기장 ID에 해당하는 일기장 삭제 성공")
  void deleteDiaryByIdsSuccess() {
    System.out.println("member.getId() = " + member.getId());
    System.out.println("diary.getId() = " + diary.getId());
    assertThat(diaryRepository.deleteByIds(member.getId(), diary.getId()))
        .as("memberId와 diaryId가 일치하는 diary가 없습니다.")
        .isEqualTo(1);
  }

  @Test
  @DisplayName("요청한 멤버 ID 및 일기장 ID에 해당하는 일기장 삭제 실패")
  void deleteDiaryByIdsFail() {
    long falseId = diary.getId() + 1;
    assertThat(diaryRepository.deleteByIds(member.getId(), falseId)).isEqualTo(0);

    falseId = member.getId() + 1;
    assertThat(diaryRepository.deleteByIds(falseId, diary.getId())).isEqualTo(0);
  }

  @Test
  @DisplayName("멤버 ID 및 시작/끝 날짜에 해당하는 데이터 가져오기")
  void findDiaryByMemberIdAndDates() {
    diaryRepository.delete(diary);
    DiaryEntity diary1 = BaseTestObject.createDiaryNoIdWithMember(member);
    DiaryEntity diary2 = BaseTestObject.createDiaryNoIdWithMember(member);
    diaryRepository.save(diary1);
    diaryRepository.save(diary2);

    Instant testStart = InstantConverter.toMonthStartDateTime(diary1.getDiaryCreatedTime());
    Instant testEnd = InstantConverter.toMonthEndDateTime(diary1.getDiaryCreatedTime());

    List<DiaryIndexProjection> resultIndex =
        diaryRepository
            .findIndexesByMemberIdAndDateTimes(member.getId(), testStart, testEnd)
            .orElseGet(List::of);
    assertThat(resultIndex).as("메서드 응답이 null 입니다.").isNotNull();

    DiaryIndexProjection first = resultIndex.get(0);
    assertThat(first).as("first 인덱스 결과가 null 입니다.").isNotNull();
    DiaryIndexProjection second = resultIndex.get(1);
    assertThat(second).as("second 인덱스 결과가 null 입니다.").isNotNull();

    assertThat(first.getId()).as("Diary1 ID와 응답 Diary ID가 서로 다릅니다.").isEqualTo(diary1.getId());

    log.info("from Java LocalDateTime Serialize = {}", diary1.getDiaryCreatedTime());
    log.info("from RDB TIMESTAMP Serialize = {}", first.getDiaryCreatedTime());

    assertThat((first.getDiaryCreatedTime()))
        .as("Diary1 Time과 응답 Diary Time이 서로 다릅니다.")
        .isEqualTo(diary1.getDiaryCreatedTime());

    assertThat(second.getId()).as("Diary2 ID와 응답 Diary ID가 서로 다릅니다.").isEqualTo(diary2.getId());

    assertThat((second.getDiaryCreatedTime()))
        .as("Diary2 Time과 응답 Diary Time이 서로 다릅니다.")
        .isEqualTo(diary2.getDiaryCreatedTime());
  }

  @Test
  @DisplayName("멤버 ID 및 시작/끝 날짜에 해당하는 데이터 가져오기 - 0건 조회")
  void findDiaryByMemberIdAndDatesZero() {
    diaryRepository.delete(diary);
    int testYear = 2023;
    int testMonth = 10;

    Instant testStart =
        YearMonth.of(testYear, testMonth).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);

    Instant testEnd =
        YearMonth.of(testYear, testMonth)
            .atEndOfMonth()
            .atTime(LocalTime.MAX)
            .toInstant(ZoneOffset.UTC);

    List<DiaryIndexProjection> resultIndex =
        diaryRepository
            .findIndexesByMemberIdAndDateTimes(member.getId(), testStart, testEnd)
            .orElseGet(List::of);
    assertThat(resultIndex).as("메서드 응답이 null 입니다.").isNotNull();

    assertThat(resultIndex.isEmpty()).as("메서드 응답 내부가 비어있지 않습니다.").isTrue();
  }

  @Test
  @DisplayName("findByMemberIdAndDate - 일기를 성공적으로 조회")
  void findByMemberIdAndDateSuccess() {
    LocalDate diaryDate = InstantConverter.toLocalDate(diary.getDiaryCreatedTime());

    Optional<DiaryEntity> result = diaryRepository.findByMemberIdAndDate(member.getId(), diaryDate);

    assertThat(result).as("해당 날짜(%s)에 작성된 일기를 찾을 수 없습니다.", diaryDate).isPresent();
    assertThat(result.get().getId())
        .as(
            "조회된 일기의 ID가 저장된 일기의 ID와 일치하지 않습니다. expected: %d, actual: %d",
            diary.getId(), result.get().getId())
        .isEqualTo(diary.getId());
    assertThat(result.get().getMemberEntity().getId())
        .as(
            "조회된 일기의 작성자 ID가 예상한 작성자 ID와 일치하지 않습니다. expected: %d, actual: %d",
            member.getId(), result.get().getMemberEntity().getId())
        .isEqualTo(member.getId());
    assertThat(result.get().getDiaryCreatedTime())
        .as(
            "조회된 일기의 작성 날짜가 예상한 날짜와 일치하지 않습니다. expected: %s, actual: %s",
            diaryDate, result.get().getDiaryCreatedTime())
        .isEqualTo(diary.getDiaryCreatedTime());
  }

  @Test
  @DisplayName("findByMemberIdAndDate - 해당 날짜에 일기가 없는 경우")
  void findByMemberIdAndDateEmpty() {
    LocalDate differentDate =
        InstantConverter.toLocalDate(diary.getDiaryCreatedTime()).plusDays(-1);

    Optional<DiaryEntity> result =
        diaryRepository.findByMemberIdAndDate(member.getId(), differentDate);

    assertThat(result).as("존재하지 않아야 할 날짜(%s)에 일기가 조회되었습니다.", differentDate).isEmpty();
  }

  @Test
  @DisplayName("Audit 기능 활성화 확인.")
  void auditJpaEntity() {
    assertThat(diary.getCreatedBy()).as("Audit이 설정 되어 있지만, DB에는 null이 들어가 있습니다.").isEqualTo(7L);
    assertThat(diary.getCreatedTime()).as("Audit이 설정 되어 있지만, DB에는 null이 들어가 있습니다.").isNotNull();
    log.info("test diary createdBy = {}", diary.getCreatedBy());
    log.info("test diary createdTime = {}", diary.getCreatedTime());
  }

  @EnableJpaAuditing
  @TestConfiguration
  static class TestJpaConfig {
    @Bean
    AuditorAware<Long> auditorAware() {
      return () -> Optional.of(7L);
    }
  }
}
