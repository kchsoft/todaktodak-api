package com.heartsave.todaktodak_api.ai.webhook.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiWebtoonRequest;
import com.heartsave.todaktodak_api.ai.webhook.repository.AiRepository;
import java.time.LocalDate;
import java.util.Optional;
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

  private AiWebtoonRequest request;
  private final Long memberId = 1L;
  private final LocalDate createdDate = LocalDate.now();
  private final String webtoonUrl = "http://example.com/webtoon/folder";

  @BeforeEach
  void setUp() {
    request = new AiWebtoonRequest(memberId, createdDate, webtoonUrl);
  }

  @Nested
  @DisplayName("웹툰 URL 저장 테스트")
  class SaveWebtoonTest {

    @Test
    @DisplayName("웹툰 URL 업데이트 성공 및 AI 컨텐츠 생성이 미완료된 경우")
    void saveWebtoon_UpdateSuccessAndAiContentCompleted() {
      when(aiRepository.updateWebtoonUrl(memberId, createdDate, webtoonUrl)).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(Optional.of(false));

      aiDiaryService.saveWebtoon(request);

      verify(aiRepository, times(1)).updateWebtoonUrl(memberId, createdDate, webtoonUrl);
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
    }

    @Test
    @DisplayName("웹툰 URL 업데이트 성공 및 AI 컨텐츠 생성이 완료된 경우")
    void saveWebtoon_UpdateSuccessAndCustomBgmUrl() {
      when(aiRepository.updateWebtoonUrl(memberId, createdDate, webtoonUrl)).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(Optional.of(true));

      aiDiaryService.saveWebtoon(request);

      verify(aiRepository, times(1)).updateWebtoonUrl(memberId, createdDate, webtoonUrl);
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
      // Todo: SSE 알림 관련 검증 추가 필요
    }

    @Test
    @DisplayName("업데이트할 일기가 없는 경우")
    void saveWebtoon_NoDataToUpdate() {
      when(aiRepository.updateWebtoonUrl(memberId, createdDate, webtoonUrl)).thenReturn(0);

      aiDiaryService.saveWebtoon(request);

      verify(aiRepository, times(1)).updateWebtoonUrl(memberId, createdDate, webtoonUrl);
      verify(aiRepository, never()).isContentCompleted(any(), any());
    }
  }
}
