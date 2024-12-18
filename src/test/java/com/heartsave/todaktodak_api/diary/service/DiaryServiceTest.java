package com.heartsave.todaktodak_api.diary.service;

import static com.heartsave.todaktodak_api.common.constant.CoreConstant.HEADER.DEFAULT_TIME_ZONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.ai.client.dto.response.AiDiaryContentResponse;
import com.heartsave.todaktodak_api.ai.client.service.AiClientService;
import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.diary.common.TestDiaryObjectFactory;
import com.heartsave.todaktodak_api.diary.constant.DiaryBgmGenre;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryResponse;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryYearMonthResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryYearMonthProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryDailyWritingLimitExceedException;
import com.heartsave.todaktodak_api.diary.exception.DiaryException;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class DiaryServiceTest {

  @Mock private MemberRepository mockMemberRepository;
  @Mock private DiaryRepository mockDiaryRepository;
  @Mock private DiaryReactionRepository mockDiaryReactionRepository;
  @Mock private AiClientService mockAiClientService;
  @Mock private S3FileStorageManager mockS3Manager;
  @InjectMocks private DiaryService diaryService;
  private MemberEntity member;
  private DiaryEntity diary;
  private final Instant NOW_DATE_TIME = Instant.now();

  @BeforeEach
  void allSetup() {
    member = BaseTestObject.createMember();
    diary = BaseTestObject.createDiaryWithMember(member);
  }

  @Test
  @DisplayName("일기 작성 성공")
  void diaryWritingSuccess() {
    DiaryWriteRequest request =
        new DiaryWriteRequest(
            NOW_DATE_TIME, DiaryEmotion.HAPPY, "test diary content", DiaryBgmGenre.ACOUSTIC);
    String AI_COMMENT = "this is test ai comment";

    when(mockMemberRepository.findById(anyLong())).thenReturn(Optional.of(member));
    when(mockDiaryRepository.existsByMemberEntity_IdAndDiaryCreatedTimeBetween(
            anyLong(), any(Instant.class), any(Instant.class)))
        .thenReturn(false);
    when(mockAiClientService.callDiaryContent(any(DiaryEntity.class)))
        .thenReturn(AiDiaryContentResponse.builder().aiComment("this is test ai comment").build());

    DiaryWriteResponse write = diaryService.write(member.getId(), request, DEFAULT_TIME_ZONE);
    assertThat(write.getAiComment()).as("AI 코멘트 결과에 문제가 발생했습니다.").isEqualTo(AI_COMMENT);
  }

  @Test
  @DisplayName("하루 일기 작성 횟수 초과 에러 발생")
  void dailyDiaryWritingLimitException() {
    DiaryWriteRequest request =
        new DiaryWriteRequest(
            NOW_DATE_TIME, DiaryEmotion.HAPPY, "test diary content", DiaryBgmGenre.POP);
    when(mockMemberRepository.findById(anyLong())).thenReturn(Optional.of(member));
    when(mockDiaryRepository.existsByMemberEntity_IdAndDiaryCreatedTimeBetween(
            anyLong(), any(Instant.class), any(Instant.class)))
        .thenReturn(true);

    DiaryException diaryException =
        assertThrows(
            DiaryDailyWritingLimitExceedException.class,
            () -> diaryService.write(member.getId(), request, DEFAULT_TIME_ZONE));
    assertThat(diaryException.getErrorSpec()).isEqualTo(DiaryErrorSpec.DAILY_WRITING_LIMIT_EXCEED);
    log.info(diaryException.getLogMessage());
  }

  @Test
  @DisplayName("일기 삭제 요청 성공")
  void diaryDeleteSuccess() {
    doNothing().when(mockDiaryRepository).delete(any(DiaryEntity.class));
    when(mockDiaryRepository.findById(diary.getId())).thenReturn(Optional.of(diary));
    doNothing().when(mockS3Manager).deleteDiaryContents(any(List.class));
    assertDoesNotThrow(
        () -> {
          diaryService.delete(member.getId(), diary.getId());
        },
        "예상치 못한 예외가 발생했습니다.");
    verify(mockDiaryRepository, times(1)).delete(any(DiaryEntity.class));
    verify(mockDiaryRepository, times(1)).findById(anyLong());
  }

  @Test
  @DisplayName("일기 삭제 요청 실패 - 일기가 없습니다.")
  void diaryDeleteFail_NotFound() {
    when(mockDiaryRepository.findById(diary.getId())).thenReturn(Optional.empty());

    DiaryException diaryException =
        assertThrows(
            DiaryNotFoundException.class,
            () -> {
              diaryService.delete(member.getId(), diary.getId());
            },
            "Diary Not Found 예외가 발생하지 않았습니다.");
    verify(mockDiaryRepository, times(0)).delete(any(DiaryEntity.class));
    verify(mockDiaryRepository, times(1)).findById(anyLong());
    log.info(diaryException.getLogMessage());
  }

  @Test
  @DisplayName("일기 삭제 요청 실패 - 찾을 수 없는 일기 입니다.")
  void diaryDeleteFail_NotDiaryOwner() {
    when(mockDiaryRepository.findById(anyLong())).thenReturn(Optional.empty());

    DiaryException diaryException =
        assertThrows(
            DiaryNotFoundException.class,
            () -> {
              diaryService.delete(member.getId() + 9999, diary.getId());
            },
            "Diary Not Found 예외가 발생하지 않았습니다.");
    verify(mockDiaryRepository, never()).deleteByIds(anyLong(), anyLong());
    verify(mockDiaryRepository, times(1)).findById(anyLong());
    log.info(diaryException.getLogMessage());
  }

  @Test
  @DisplayName("연월 일기 작성 현황 조회 1건 이상 성공")
  void yearMonthMoreThanOneSuccess() {
    int testYear = 2024;
    int testMonth = 3;
    Instant testStart =
        YearMonth.of(testYear, testMonth)
            .atDay(1)
            .atStartOfDay(ZoneId.of(DEFAULT_TIME_ZONE))
            .toInstant();
    Instant testEnd =
        YearMonth.of(testYear, testMonth)
            .atEndOfMonth()
            .atTime(LocalTime.MAX)
            .atZone(ZoneId.of(DEFAULT_TIME_ZONE))
            .toInstant();
    List<DiaryYearMonthProjection> testProjection =
        TestDiaryObjectFactory.getTestDiaryIndexProjections_2024_03_Data_Of_2();
    Instant requestYearMonth =
        LocalDate.of(testYear, testMonth, 1).atStartOfDay(ZoneId.of(DEFAULT_TIME_ZONE)).toInstant();

    when(mockDiaryRepository
            .findByMemberEntity_IdAndDiaryCreatedTimeBetweenOrderByDiaryCreatedTimeDesc(
                member.getId(), testStart, testEnd))
        .thenReturn(testProjection);
    DiaryYearMonthResponse response =
        diaryService.getYearMonth(member.getId(), requestYearMonth, DEFAULT_TIME_ZONE);

    List<DiaryYearMonthProjection> responseYearMonth = response.getDiaryYearMonths();
    DiaryYearMonthProjection first = responseYearMonth.get(0);
    DiaryYearMonthProjection second = responseYearMonth.get(1);

    assertThat(first.getId())
        .as("설정 Diary ID와 응답 DiaryID가 서로 다릅니다.")
        .isEqualTo(testProjection.get(0).getId());

    assertThat((first.getDiaryCreatedTime()))
        .as("설정 Diary Time과 응답 Diary Time이 서로 다릅니다.")
        .isEqualTo(testProjection.get(0).getDiaryCreatedTime());

    assertThat(second.getId())
        .as("설정 Diary ID와 응답 DiaryID가 서로 다릅니다.")
        .isEqualTo(testProjection.get(1).getId());

    assertThat((second.getDiaryCreatedTime()))
        .as("설정 Diary Time과 응답 Diary Time이 서로 다릅니다.")
        .isEqualTo(testProjection.get(1).getDiaryCreatedTime());
  }

  @Test
  @DisplayName("연월 일기 작성 현황 조회 0건 성공")
  void yearMonthZeroSuccess() {
    int testYear = 2024;
    int testMonth = 2;
    Instant testStart =
        YearMonth.of(testYear, testMonth)
            .atDay(1)
            .atStartOfDay(ZoneId.of(DEFAULT_TIME_ZONE))
            .toInstant();
    Instant testEnd =
        YearMonth.of(testYear, testMonth)
            .atEndOfMonth()
            .atTime(LocalTime.MAX)
            .atZone(ZoneId.of(DEFAULT_TIME_ZONE))
            .toInstant();
    Instant requestYearMonth =
        LocalDate.of(testYear, testMonth, 1).atStartOfDay(ZoneId.of(DEFAULT_TIME_ZONE)).toInstant();
    when(mockDiaryRepository
            .findByMemberEntity_IdAndDiaryCreatedTimeBetweenOrderByDiaryCreatedTimeDesc(
                member.getId(), testStart, testEnd))
        .thenReturn(List.of());

    DiaryYearMonthResponse yearMonths =
        diaryService.getYearMonth(member.getId(), requestYearMonth, DEFAULT_TIME_ZONE);

    List<DiaryYearMonthProjection> yearMonthProjection = yearMonths.getDiaryYearMonths();
    System.out.println("yearMonthProjection = " + yearMonthProjection);
    assertThat(yearMonthProjection).as("응답이 null 입니다.").isNotNull();
    assertThat(yearMonthProjection.size()).as("응답 내용의 크기가 0이 아닙니다.").isEqualTo(0);
  }

  @Test
  @DisplayName("일기 상세 조회 성공")
  void getDiarySuccess() {
    // given
    Instant requestDate = NOW_DATE_TIME;
    when(mockDiaryRepository.findDiaryEntityByMemberEntity_IdAndDiaryCreatedTimeBetween(
            anyLong(), any(Instant.class), any(Instant.class)))
        .thenReturn(Optional.of(diary));
    List<String> preSignedWebtoon = List.of("pre-sigend-webtoon");
    String preSignedBgm = "pre-sigend-bgm";

    when(mockS3Manager.preSignedWebtoonUrlFrom(anyList())).thenReturn(preSignedWebtoon);
    when(mockS3Manager.preSignedBgmUrlFrom(anyString())).thenReturn(preSignedBgm);

    DiaryResponse response = diaryService.getDiary(member.getId(), requestDate, DEFAULT_TIME_ZONE);

    assertThat(response)
        .satisfies(
            r -> {
              assertThat(r.getDiaryId()).isEqualTo(diary.getId());
              assertThat(r.getEmotion()).isEqualTo(diary.getEmotion());
              assertThat(r.getContent()).isEqualTo(diary.getContent());
              assertThat(r.getWebtoonImageUrls()).isEqualTo(preSignedWebtoon);
              assertThat(r.getBgmUrl()).isEqualTo(preSignedBgm);
              assertThat(r.getAiComment()).isEqualTo(diary.getAiComment());
              assertThat(r.getDate()).isEqualTo(diary.getDiaryCreatedTime());
            });

    verify(mockDiaryRepository, times(1))
        .findDiaryEntityByMemberEntity_IdAndDiaryCreatedTimeBetween(
            anyLong(), any(Instant.class), any(Instant.class));
  }

  @Test
  @DisplayName("일기 상세 조회 실패 - 일기를 찾을 수 없음")
  void getDiaryFail() {
    Instant requestDate = NOW_DATE_TIME;

    when(mockDiaryRepository.findDiaryEntityByMemberEntity_IdAndDiaryCreatedTimeBetween(
            anyLong(), any(Instant.class), any(Instant.class)))
        .thenReturn(Optional.empty());

    DiaryException diaryException =
        assertThrows(
            DiaryNotFoundException.class,
            () -> diaryService.getDiary(member.getId(), requestDate, DEFAULT_TIME_ZONE));

    assertThat(diaryException.getErrorSpec()).isEqualTo(DiaryErrorSpec.DIARY_NOT_FOUND);
    verify(mockDiaryRepository, times(1))
        .findDiaryEntityByMemberEntity_IdAndDiaryCreatedTimeBetween(
            anyLong(), any(Instant.class), any(Instant.class));
    verify(mockDiaryReactionRepository, times(0)).countEachByPublicDiaryId(anyLong());
  }
}
