package com.heartsave.todaktodak_api.diary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.ai.dto.AiContentResponse;
import com.heartsave.todaktodak_api.ai.service.AiService;
import com.heartsave.todaktodak_api.common.BaseTestEntity;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.common.storage.S3FileStorageService;
import com.heartsave.todaktodak_api.diary.common.TestDiaryObjectFactory;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryIndexResponse;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryResponse;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.entity.projection.DiaryIndexProjection;
import com.heartsave.todaktodak_api.diary.exception.DiaryDailyWritingLimitExceedException;
import com.heartsave.todaktodak_api.diary.exception.DiaryDeleteNotFoundException;
import com.heartsave.todaktodak_api.diary.exception.DiaryException;
import com.heartsave.todaktodak_api.diary.exception.DiaryNotFoundException;
import com.heartsave.todaktodak_api.diary.repository.DiaryReactionRepository;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
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
  @Mock private AiService mockAiService;
  @Mock private S3FileStorageService s3FileStorageService;
  @InjectMocks private DiaryService diaryService;
  private TodakUser principal;
  private MemberEntity member;
  private DiaryEntity diary;
  private final LocalDateTime NOW_DATE_TIME = LocalDateTime.now();

  @BeforeEach
  void allSetup() {
    member = BaseTestEntity.createMember();
    diary = BaseTestEntity.createDiaryWithMember(member);

    principal = mock(TodakUser.class);
    when(principal.getId()).thenReturn(member.getId());
  }

  @Test
  @DisplayName("일기 작성 성공")
  void diaryWritingSuccess() {
    DiaryWriteRequest request =
        new DiaryWriteRequest(NOW_DATE_TIME, DiaryEmotion.JOY, "test diary content");
    String AI_COMMENT = "this is test ai comment";

    when(mockMemberRepository.findById(anyLong())).thenReturn(Optional.of(member));
    when(mockDiaryRepository.existsByDate(anyLong(), any(LocalDateTime.class))).thenReturn(false);
    when(mockAiService.callAiContent(any(DiaryEntity.class)))
        .thenReturn(AiContentResponse.builder().aiComment("this is test ai comment").build());

    DiaryWriteResponse write = diaryService.write(principal, request);
    assertThat(write.getAiComment()).as("AI 코멘트 결과에 문제가 발생했습니다.").isEqualTo(AI_COMMENT);
  }

  @Test
  @DisplayName("하루 일기 작성 횟수 초과 에러 발생")
  void dailyDiaryWritingLimitException() {
    DiaryWriteRequest request =
        new DiaryWriteRequest(NOW_DATE_TIME, DiaryEmotion.JOY, "test diary content");
    when(mockMemberRepository.findById(anyLong())).thenReturn(Optional.of(member));
    when(mockDiaryRepository.existsByDate(anyLong(), any(LocalDateTime.class))).thenReturn(true);

    DiaryException diaryException =
        assertThrows(
            DiaryDailyWritingLimitExceedException.class,
            () -> diaryService.write(principal, request));
    assertThat(diaryException.getErrorSpec()).isEqualTo(DiaryErrorSpec.DAILY_WRITING_LIMIT_EXCEED);
    log.info(diaryException.getLogMessage());
  }

  @Test
  @DisplayName("일기 삭제 요청 성공")
  void diaryDeleteSuccess() {
    when(mockDiaryRepository.deleteByIds(anyLong(), anyLong())).thenReturn(1);
    assertDoesNotThrow(
        () -> {
          diaryService.delete(principal, diary.getId());
        },
        "예상치 못한 예외가 발생했습니다.");
    verify(mockDiaryRepository, times(1)).deleteByIds(anyLong(), anyLong());
  }

  @Test
  @DisplayName("일기 삭제 요청 실패")
  void diaryDeleteFail() {
    when(mockDiaryRepository.deleteByIds(anyLong(), anyLong())).thenReturn(0);
    DiaryException diaryException =
        assertThrows(
            DiaryDeleteNotFoundException.class,
            () -> {
              diaryService.delete(principal, diary.getId() + 1L);
            },
            "Diary Delete Not Found 예외가 발생하지 않았습니다.");
    verify(mockDiaryRepository, times(1)).deleteByIds(anyLong(), anyLong());
    log.info(diaryException.getLogMessage());
  }

  @Test
  @DisplayName("연월 일기 작성 현황 조회 1건 이상 성공")
  void yearMonthMoreThanOneSuccess() {
    int testYear = 2024;
    int testMonth = 3;
    LocalDateTime testStart = YearMonth.of(testYear, testMonth).atDay(1).atStartOfDay();
    LocalDateTime testEnd = YearMonth.of(testYear, testMonth).atEndOfMonth().atTime(LocalTime.MAX);
    List<DiaryIndexProjection> testProjection =
        TestDiaryObjectFactory.getTestDiaryIndexProjections_2024_03_Data_Of_2();

    when(mockDiaryRepository.findIndexesByMemberIdAndDateTimes(
            principal.getId(), testStart, testEnd))
        .thenReturn(Optional.of(testProjection));

    DiaryIndexResponse indexes =
        diaryService.getIndex(principal, YearMonth.of(testYear, testMonth));

    List<DiaryIndexProjection> responseIndexes = indexes.getDiaryIndexes();
    DiaryIndexProjection first = responseIndexes.get(0);
    DiaryIndexProjection second = responseIndexes.get(1);

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
    LocalDateTime testStart = YearMonth.of(testYear, testMonth).atDay(1).atStartOfDay();
    LocalDateTime testEnd = YearMonth.of(testYear, testMonth).atEndOfMonth().atTime(LocalTime.MAX);

    when(mockDiaryRepository.findIndexesByMemberIdAndDateTimes(
            principal.getId(), testStart, testEnd))
        .thenReturn(Optional.empty());

    DiaryIndexResponse indexes =
        diaryService.getIndex(principal, YearMonth.of(testYear, testMonth));

    List<DiaryIndexProjection> responseIndexes = indexes.getDiaryIndexes();
    System.out.println("responseIndexes = " + responseIndexes);
    assertThat(responseIndexes).as("응답이 null 입니다.").isNotNull();
    assertThat(responseIndexes.size()).as("응답 내용의 크기가 0이 아닙니다.").isEqualTo(0);
  }

  @Test
  @DisplayName("일기 상세 조회 성공")
  void getDiarySuccess() {
    // given
    LocalDate requestDate = NOW_DATE_TIME.toLocalDate();
    when(mockDiaryRepository.findByMemberIdAndDate(anyLong(), any(LocalDate.class)))
        .thenReturn(Optional.of(diary));
    List<String> preSignedWebtoon = List.of("pre-sigend-webtoon");
    String preSignedBgm = "pre-sigend-bgm";

    when(s3FileStorageService.preSignedWebtoonUrlFrom(anyList())).thenReturn(preSignedWebtoon);
    when(s3FileStorageService.preSignedBgmUrlFrom(anyString())).thenReturn(preSignedBgm);

    DiaryResponse response = diaryService.getDiary(principal, requestDate);

    assertThat(response)
        .satisfies(
            r -> {
              assertThat(r.getDiaryId()).isEqualTo(diary.getId());
              assertThat(r.getEmotion()).isEqualTo(diary.getEmotion());
              assertThat(r.getContent()).isEqualTo(diary.getContent());
              assertThat(r.getWebtoonImageUrls()).isEqualTo(preSignedWebtoon);
              assertThat(r.getBgmUrl()).isEqualTo(preSignedBgm);
              assertThat(r.getAiComment()).isEqualTo(diary.getAiComment());
              assertThat(r.getDate()).isEqualTo(diary.getDiaryCreatedTime().toLocalDate());
            });

    verify(mockDiaryRepository, times(1)).findByMemberIdAndDate(anyLong(), any(LocalDate.class));
  }

  @Test
  @DisplayName("일기 상세 조회 실패 - 일기를 찾을 수 없음")
  void getDiaryFail() {
    LocalDate requestDate = NOW_DATE_TIME.toLocalDate();

    when(mockDiaryRepository.findByMemberIdAndDate(anyLong(), any(LocalDate.class)))
        .thenReturn(Optional.empty());

    DiaryException diaryException =
        assertThrows(
            DiaryNotFoundException.class, () -> diaryService.getDiary(principal, requestDate));

    assertThat(diaryException.getErrorSpec()).isEqualTo(DiaryErrorSpec.DIARY_NOT_FOUND);
    verify(mockDiaryRepository, times(1)).findByMemberIdAndDate(anyLong(), any(LocalDate.class));
    verify(mockDiaryReactionRepository, times(0)).countEachByDiaryId(anyLong());
  }
}
