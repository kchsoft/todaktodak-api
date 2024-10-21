package com.heartsave.todaktodak_api.diary.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.heartsave.todaktodak_api.ai.dto.AiContentResponse;
import com.heartsave.todaktodak_api.ai.service.AiService;
import com.heartsave.todaktodak_api.common.exception.BaseException;
import com.heartsave.todaktodak_api.common.type.ErrorSpec;
import com.heartsave.todaktodak_api.diary.constant.DiaryEmotion;
import com.heartsave.todaktodak_api.diary.dto.request.DiaryWriteRequest;
import com.heartsave.todaktodak_api.diary.dto.response.DiaryWriteResponse;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.diary.repository.DiaryRepository;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
public class DiaryServiceTest {

  @Mock private MemberRepository memberRepository;
  @Mock private DiaryRepository diaryRepository;
  @Mock private AiService aiService;
  @InjectMocks private DiaryService diaryService;
  private static OAuth2User oAuth2User;
  private static DiaryWriteRequest request;
  private static MemberEntity member;
  private String AI_COMMENT = "this is test ai comment";
  private static final LocalDateTime FIXED_DATE = LocalDateTime.of(2024, 10, 21, 14, 14);

  @BeforeAll
  static void allSetup() {
    oAuth2User = mock(OAuth2User.class);
    request = mock(DiaryWriteRequest.class);

    when(oAuth2User.getName()).thenReturn("2");
    member = MemberEntity.builder().id(2L).build();

    when(request.getContent()).thenReturn("test diary content");
    when(request.getEmotion()).thenReturn(DiaryEmotion.JOY);
    when(request.getDate()).thenReturn(FIXED_DATE);
  }

  @BeforeEach
  void eachSetup() {
    when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
  }

  @Test
  @DisplayName("일기 작성 성공")
  void diaryWritingSuccess() {
    when(diaryRepository.existsByDate(anyLong(), any(LocalDateTime.class))).thenReturn(false);
    when(aiService.callAiContent(any(DiaryEntity.class)))
        .thenReturn(AiContentResponse.builder().aiComment("this is test ai comment").build());

    DiaryWriteResponse write = diaryService.write(oAuth2User, request);
    assertThat(write.getAiComment()).as("AI 코멘트 결과에 문제가 발생했습니다.").isEqualTo(AI_COMMENT);
  }

  @Test
  @DisplayName("하루 일기 작성 횟수 초과 에러 발생")
  void dailyDiaryWritingLimitException() {
    when(diaryRepository.existsByDate(anyLong(), any(LocalDateTime.class))).thenReturn(true);

    BaseException baseException =
        assertThrows(BaseException.class, () -> diaryService.write(oAuth2User, request));
    assertThat(baseException.getErrorSpec())
        .isEqualTo(ErrorSpec.DIARY_DAILY_WRITING_LIMIT_EXCEPTION);
  }
}
