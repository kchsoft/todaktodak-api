package com.heartsave.todaktodak_api.ai.webhook.service;

import static com.heartsave.todaktodak_api.common.BaseTestObject.TEST_BGM_URL;
import static com.heartsave.todaktodak_api.common.BaseTestObject.TEST_WEBTOON_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookBgmCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookWebtoonCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.repository.AiRepository;
import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageService;
import com.heartsave.todaktodak_api.event.service.EventService;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.repository.MemberRepository;
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
  @Mock private S3FileStorageService s3FileStorageService;
  @Mock private MemberRepository memberRepository;
  @Mock private EventService eventService;
  @InjectMocks private AiDiaryService aiDiaryService;

  private WebhookWebtoonCompletionRequest webtoonRequest;
  private WebhookBgmCompletionRequest bgmRequest;
  private final Long memberId = 1L;
  private final LocalDate createdDate = LocalDate.now();
  private final String parseKeyUrl = "parse-key-url";

  @BeforeEach
  void setUp() {
    webtoonRequest = new WebhookWebtoonCompletionRequest(memberId, createdDate, TEST_WEBTOON_URL);
    bgmRequest = new WebhookBgmCompletionRequest(memberId, createdDate, TEST_BGM_URL);
    when(s3FileStorageService.parseKeyFrom(anyString())).thenReturn(parseKeyUrl);
  }

  @Nested
  @DisplayName("웹툰 URL 저장 테스트")
  class SaveWebtoonTest {

    @Test
    @DisplayName("웹툰 URL 업데이트 성공 및 AI 컨텐츠 생성이 미완료된 경우")
    void saveWebtoon_UpdateSuccessAndAiContentCompleted() {
      when(aiRepository.updateWebtoonUrl(webtoonRequest, parseKeyUrl)).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(false);

      aiDiaryService.saveWebtoon(webtoonRequest);

      verify(aiRepository, times(1)).updateWebtoonUrl(webtoonRequest, parseKeyUrl);
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
    }

    @Test
    @DisplayName("웹툰 URL 업데이트 성공 및 AI 컨텐츠 생성이 완료된 경우")
    void saveWebtoon_UpdateSuccessAndCustomBgmUrl() {
      when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mock(MemberEntity.class)));
      when(aiRepository.updateWebtoonUrl(webtoonRequest, parseKeyUrl)).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(true);

      aiDiaryService.saveWebtoon(webtoonRequest);

      verify(aiRepository, times(1)).updateWebtoonUrl(webtoonRequest, parseKeyUrl);
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
      // Todo: SSE 알림 관련 검증 추가 필요
    }

    @Test
    @DisplayName("업데이트할 일기가 없는 경우")
    void saveWebtoon_NoDataToUpdate() {
      when(aiRepository.updateWebtoonUrl(webtoonRequest, parseKeyUrl)).thenReturn(0);

      aiDiaryService.saveWebtoon(webtoonRequest);

      verify(aiRepository, times(1)).updateWebtoonUrl(webtoonRequest, parseKeyUrl);
      verify(aiRepository, never()).isContentCompleted(any(), any());
    }
  }

  @Nested
  @DisplayName("BGM URL 저장 테스트")
  class SaveBgmTest {

    @Test
    @DisplayName("BGM URL 업데이트 성공 및 AI 컨텐츠 생성이 미완료된 경우")
    void saveBgm_UpdateSuccessAndAiContentNotCompleted() {
      when(aiRepository.updateBgmUrl(bgmRequest, parseKeyUrl)).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(false);

      aiDiaryService.saveBgm(bgmRequest);

      verify(aiRepository, times(1)).updateBgmUrl(bgmRequest, parseKeyUrl);
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
    }

    @Test
    @DisplayName("BGM URL 업데이트 성공 및 AI 컨텐츠 생성이 완료된 경우")
    void saveBgm_UpdateSuccessAndAiContentCompleted() {
      when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mock(MemberEntity.class)));
      when(aiRepository.updateBgmUrl(bgmRequest, parseKeyUrl)).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(true);

      aiDiaryService.saveBgm(bgmRequest);

      verify(aiRepository, times(1)).updateBgmUrl(bgmRequest, parseKeyUrl);
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
      // Todo: SSE 알림 관련 검증 추가 필요
    }

    @Test
    @DisplayName("업데이트할 일기가 없는 경우")
    void saveBgm_NoDataToUpdate() {
      when(aiRepository.updateBgmUrl(bgmRequest, parseKeyUrl)).thenReturn(0);

      aiDiaryService.saveBgm(bgmRequest);

      verify(aiRepository, times(1)).updateBgmUrl(bgmRequest, parseKeyUrl);
      verify(aiRepository, never()).isContentCompleted(any(), any());
    }
  }
}
