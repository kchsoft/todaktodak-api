package com.heartsave.todaktodak_api.domain.ai.webhook.service;

import static com.heartsave.todaktodak_api.config.BaseTestObject.TEST_BGM_KEY_URL;
import static com.heartsave.todaktodak_api.config.BaseTestObject.TEST_WEBTOON_KEY_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heartsave.todaktodak_api.common.storage.s3.S3FileStorageManager;
import com.heartsave.todaktodak_api.domain.ai.callback.domain.AiCallbackBgmCompletion;
import com.heartsave.todaktodak_api.domain.ai.callback.domain.AiCallbackWebtoonCompletion;
import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackBgmRequest;
import com.heartsave.todaktodak_api.domain.ai.callback.dto.request.AiCallbackWebtoonRequest;
import com.heartsave.todaktodak_api.domain.ai.callback.repository.AiCallbackRepository;
import com.heartsave.todaktodak_api.domain.ai.callback.service.AiDiaryService;
import com.heartsave.todaktodak_api.domain.event.service.EventService;
import com.heartsave.todaktodak_api.domain.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.domain.member.repository.MemberRepository;
import java.time.Instant;
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

  @Mock private AiCallbackRepository aiRepository;
  @Mock private S3FileStorageManager s3FileStorageManager;
  @Mock private MemberRepository memberRepository;
  @Mock private EventService eventService;
  @InjectMocks private AiDiaryService aiDiaryService;

  private AiCallbackWebtoonRequest webtoonRequest;
  private AiCallbackBgmRequest bgmRequest;
  private final Long memberId = 1L;
  private final Instant createdDate = Instant.now();
  private final String PARSED_KEY_URL = "parsed-key-url";

  @BeforeEach
  void setUp() {
    webtoonRequest = new AiCallbackWebtoonRequest(memberId, createdDate, TEST_WEBTOON_KEY_URL);

    bgmRequest = new AiCallbackBgmRequest(memberId, createdDate, TEST_BGM_KEY_URL);

    when(s3FileStorageManager.parseKeyFrom(anyString())).thenReturn(PARSED_KEY_URL);
  }

  @Nested
  @DisplayName("웹툰 URL 저장 테스트")
  class SaveWebtoonTest {

    @Test
    @DisplayName("웹툰 URL 업데이트 성공 및 AI 컨텐츠 생성이 미완료된 경우")
    void saveWebtoon_UpdateSuccessAndAiContentCompleted() {
      when(aiRepository.updateWebtoonUrl(any(AiCallbackWebtoonCompletion.class))).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(false);

      aiDiaryService.saveWebtoon(webtoonRequest);

      verify(aiRepository, times(1)).updateWebtoonUrl(any(AiCallbackWebtoonCompletion.class));
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
    }

    @Test
    @DisplayName("웹툰 URL 업데이트 성공 및 AI 컨텐츠 생성이 완료된 경우")
    void saveWebtoon_UpdateSuccessAndCustomBgmUrl() {
      when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mock(MemberEntity.class)));
      when(aiRepository.updateWebtoonUrl(any(AiCallbackWebtoonCompletion.class))).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(true);

      aiDiaryService.saveWebtoon(webtoonRequest);

      verify(aiRepository, times(1)).updateWebtoonUrl(any(AiCallbackWebtoonCompletion.class));
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
      // Todo: SSE 알림 관련 검증 추가 필요
    }

    @Test
    @DisplayName("업데이트할 일기가 없는 경우")
    void saveWebtoon_NoDataToUpdate() {
      when(aiRepository.updateWebtoonUrl(any(AiCallbackWebtoonCompletion.class))).thenReturn(0);

      aiDiaryService.saveWebtoon(webtoonRequest);

      verify(aiRepository, times(1)).updateWebtoonUrl(any(AiCallbackWebtoonCompletion.class));
      verify(aiRepository, never()).isContentCompleted(any(), any());
    }
  }

  @Nested
  @DisplayName("BGM URL 저장 테스트")
  class SaveBgmTest {

    @Test
    @DisplayName("BGM URL 업데이트 성공 및 AI 컨텐츠 생성이 미완료된 경우")
    void saveBgm_UpdateSuccessAndAiContentNotCompleted() {
      when(aiRepository.updateBgmUrl(any(AiCallbackBgmCompletion.class))).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(false);

      aiDiaryService.saveBgm(bgmRequest);

      verify(aiRepository, times(1)).updateBgmUrl(any(AiCallbackBgmCompletion.class));
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
    }

    @Test
    @DisplayName("BGM URL 업데이트 성공 및 AI 컨텐츠 생성이 완료된 경우")
    void saveBgm_UpdateSuccessAndAiContentCompleted() {
      when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mock(MemberEntity.class)));
      when(aiRepository.updateBgmUrl(any(AiCallbackBgmCompletion.class))).thenReturn(1);
      when(aiRepository.isContentCompleted(memberId, createdDate)).thenReturn(true);

      aiDiaryService.saveBgm(bgmRequest);

      verify(aiRepository, times(1)).updateBgmUrl(any(AiCallbackBgmCompletion.class));
      verify(aiRepository, times(1)).isContentCompleted(memberId, createdDate);
      // Todo: SSE 알림 관련 검증 추가 필요
    }

    @Test
    @DisplayName("업데이트할 일기가 없는 경우")
    void saveBgm_NoDataToUpdate() {
      when(aiRepository.updateBgmUrl(any(AiCallbackBgmCompletion.class))).thenReturn(0);

      aiDiaryService.saveBgm(bgmRequest);

      verify(aiRepository, times(1)).updateBgmUrl(any(AiCallbackBgmCompletion.class));
      verify(aiRepository, never()).isContentCompleted(any(), any());
    }
  }
}
