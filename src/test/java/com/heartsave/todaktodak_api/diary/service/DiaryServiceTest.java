package com.heartsave.todaktodak_api.diary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.ai.dto.AiContentResponse;
import com.heartsave.todaktodak_api.ai.service.AiService;
import com.heartsave.todaktodak_api.common.BaseTestEntity;
import com.heartsave.todaktodak_api.common.exception.errorspec.DiaryErrorSpec;
import com.heartsave.todaktodak_api.common.security.domain.TodakUser;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.exception.DiaryDailyWritingLimitExceedException;
import com.heartsave.todaktodak_api.diary.exception.DiaryDeleteNotFoundException;
import com.heartsave.todaktodak_api.diary.exception.DiaryException;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class DiaryServiceTest {

  @Mock private MemberRepository memberRepository;
  @Mock private DiaryRepository diaryRepository;
  @Mock private AiService aiService;
  @InjectMocks private DiaryService diaryService;
  private static TodakUser principal;
  private static DiaryWriteRequest request;
  private static MemberEntity member;
  private static DiaryEntity diary;
  private String AI_COMMENT = "this is test ai comment";
  private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2024, 10, 21, 14, 14);

  @BeforeAll
  static void allSetup() {
    principal = mock(TodakUser.class);
    request = new DiaryWriteRequest(FIXED_DATE, DiaryEmotion.JOY, "test diary content");

    member = BaseTestEntity.createMember();
    diary = BaseTestEntity.createDiaryWithMember(member);
    when(principal.getId()).thenReturn(member.getId());
  }

  @Test
  @DisplayName("일기 작성 성공")
  void diaryWritingSuccess() {
    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
    when(diaryRepository.existsByDate(anyLong(), any(LocalDateTime.class))).thenReturn(false);
    when(aiService.callAiContent(any(DiaryEntity.class)))
        .thenReturn(AiContentResponse.builder().aiComment("this is test ai comment").build());

    DiaryWriteResponse write = diaryService.write(principal, request);
    assertThat(write.getAiComment()).as("AI 코멘트 결과에 문제가 발생했습니다.").isEqualTo(AI_COMMENT);
  }

  @Test
  @DisplayName("하루 일기 작성 횟수 초과 에러 발생")
  void dailyDiaryWritingLimitException() {
    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
    when(diaryRepository.existsByDate(anyLong(), any(LocalDateTime.class))).thenReturn(true);

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
    when(diaryRepository.deleteByIds(anyLong(), anyLong())).thenReturn(1);
    assertDoesNotThrow(
        () -> {
          diaryService.delete(principal, diary.getId());
        },
        "예상치 못한 예외가 발생했습니다.");
    verify(diaryRepository, times(1)).deleteByIds(anyLong(), anyLong());
  }

  @Test
  @DisplayName("일기 삭제 요청 실패")
  void diaryDeleteFail() {
    when(diaryRepository.deleteByIds(anyLong(), anyLong())).thenReturn(0);
    DiaryException diaryException =
        assertThrows(
            DiaryDeleteNotFoundException.class,
            () -> {
              diaryService.delete(principal, diary.getId() + 1L);
            },
            "Diary Delete Not Found 예외가 발생하지 않았습니다.");
    verify(diaryRepository, times(1)).deleteByIds(anyLong(), anyLong());
    log.info(diaryException.getLogMessage());
  }
}
