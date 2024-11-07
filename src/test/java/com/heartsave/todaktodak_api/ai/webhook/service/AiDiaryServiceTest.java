package com.heartsave.todaktodak_api.ai.webhook.service;

import static com.heartsave.todaktodak_api.common.BaseTestObject.TEST_BGM_URL;
import static com.heartsave.todaktodak_api.common.BaseTestObject.TEST_WEBTOON_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookBgmCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookWebtoonCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.repository.AiRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiDiaryServiceTest {

  @Mock private AiRepository aiRepository;
  @InjectMocks private AiDiaryService aiDiaryService;

  private WebhookWebtoonCompletionRequest webtoonRequest;
  private WebhookBgmCompletionRequest bgmRequest;
  private final Long memberId = 1L;
  private final LocalDate createdDate = LocalDate.now();

  @BeforeEach
  void setUp() {
    webtoonRequest = new WebhookWebtoonCompletionRequest(memberId, createdDate, TEST_WEBTOON_URL);
    bgmRequest = new WebhookBgmCompletionRequest(memberId, createdDate, TEST_BGM_URL);
  }

  @Nested
  @DisplayName("웹툰 URL 저장 테스트")
  class SaveWebtoonTest {

    @Test
    @DisplayName("웹툰 URL 업데이트 성공 및 AI 컨텐츠 생성이 미완료된 경우")
    void saveWebtoon_UpdateSuccessAndAiContentCompleted() {
      when(aiRepository.updateWebtoonUrl(webtoonRequest)).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(false);

      aiDiaryService.saveWebtoon(webtoonRequest);

      verify(aiRepository, times(1)).updateWebtoonUrl(webtoonRequest);
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
    }

    @Test
    @DisplayName("웹툰 URL 업데이트 성공 및 AI 컨텐츠 생성이 완료된 경우")
    void saveWebtoon_UpdateSuccessAndCustomBgmUrl() {
      when(aiRepository.updateWebtoonUrl(webtoonRequest)).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(true);

      aiDiaryService.saveWebtoon(webtoonRequest);

      verify(aiRepository, times(1)).updateWebtoonUrl(webtoonRequest);
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
      // Todo: SSE 알림 관련 검증 추가 필요
    }

    @Test
    @DisplayName("업데이트할 일기가 없는 경우")
    void saveWebtoon_NoDataToUpdate() {
      when(aiRepository.updateWebtoonUrl(webtoonRequest)).thenReturn(0);

      aiDiaryService.saveWebtoon(webtoonRequest);

      verify(aiRepository, times(1)).updateWebtoonUrl(webtoonRequest);
      verify(aiRepository, never()).isContentCompleted(any(), any());
    }
  }

  @Nested
  @DisplayName("BGM URL 저장 테스트")
  class SaveBgmTest {

    @Test
    @DisplayName("BGM URL 업데이트 성공 및 AI 컨텐츠 생성이 미완료된 경우")
    void saveBgm_UpdateSuccessAndAiContentNotCompleted() {
      when(aiRepository.updateBgmUrl(bgmRequest)).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(false);

      aiDiaryService.saveBgm(bgmRequest);

      verify(aiRepository, times(1)).updateBgmUrl(bgmRequest);
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
    }

    @Test
    @DisplayName("BGM URL 업데이트 성공 및 AI 컨텐츠 생성이 완료된 경우")
    void saveBgm_UpdateSuccessAndAiContentCompleted() {
      when(aiRepository.updateBgmUrl(bgmRequest)).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(true);

      aiDiaryService.saveBgm(bgmRequest);

      verify(aiRepository, times(1)).updateBgmUrl(bgmRequest);
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
      // Todo: SSE 알림 관련 검증 추가 필요
    }

    @Test
    @DisplayName("업데이트할 일기가 없는 경우")
    void saveBgm_NoDataToUpdate() {
      when(aiRepository.updateBgmUrl(bgmRequest)).thenReturn(0);

      aiDiaryService.saveBgm(bgmRequest);

      verify(aiRepository, times(1)).updateBgmUrl(bgmRequest);
      verify(aiRepository, never()).isContentCompleted(any(), any());
    }
  }
}
