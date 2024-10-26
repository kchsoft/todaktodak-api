package com.heartsave.todaktodak_api.diary.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.heartsave.todaktodak_api.common.BaseTestEntity;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIndexProjection;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
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
  void setupAll() {
    member = BaseTestEntity.createMemberNoId();
    diary = BaseTestEntity.createDiaryNoIdWithMember(member);
    memberRepository.save(member);
    diaryRepository.save(diary);
  }

  @Test
  @DisplayName("특정 날짜에 해당되는 사용자 일기 없음.")
  void notExistDiaryByDateAndMember() {
    LocalDateTime testTime = LocalDateTime.of(2025, 10, 22, 11, 1);
    boolean exist = diaryRepository.existsByDate(member.getId(), testTime);
    assertThat(exist).as("memberID와 날짜에 해당하는 일기가 있습니다.").isFalse();
  }

  @Test
  @DisplayName("특정 날짜에 해당되는 사용자 일기가 있음.")
  void existDiaryByDateAndMember() {
    boolean exist = diaryRepository.existsByDate(member.getId(), diary.getDiaryCreatedTime());
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
    DiaryEntity diary1 = BaseTestEntity.createDiaryNoIdWithMember(member);
    DiaryEntity diary2 = BaseTestEntity.createDiaryNoIdWithMember(member);
    diaryRepository.save(diary1);
    diaryRepository.save(diary2);
    int testYear = diary1.getDiaryCreatedTime().getYear();
    int testMonth = diary1.getDiaryCreatedTime().getMonthValue();

    LocalDateTime testStart = YearMonth.of(testYear, testMonth).atDay(1).atStartOfDay();
    LocalDateTime testEnd = YearMonth.of(testYear, testMonth).atEndOfMonth().atTime(LocalTime.MAX);

    List<DiaryIndexProjection> resultIndex =
        diaryRepository.findIndexesByMemberIdAndDateTimes(member.getId(), testStart, testEnd);
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

    LocalDateTime testStart = YearMonth.of(testYear, testMonth).atDay(1).atStartOfDay();
    LocalDateTime testEnd = YearMonth.of(testYear, testMonth).atEndOfMonth().atTime(LocalTime.MAX);

    List<DiaryIndexProjection> resultIndex =
        diaryRepository.findIndexesByMemberIdAndDateTimes(member.getId(), testStart, testEnd);
    assertThat(resultIndex).as("메서드 응답이 null 입니다.").isNotNull();

    assertThat(resultIndex.isEmpty()).as("메서드 응답 내부가 비어있지 않습니다.").isTrue();
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
