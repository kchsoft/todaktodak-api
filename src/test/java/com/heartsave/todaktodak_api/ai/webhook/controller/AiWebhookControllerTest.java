package com.heartsave.todaktodak_api.ai.webhook.controller;

import static com.heartsave.todaktodak_api.common.BaseTestObject.TEST_BGM_URL;
import static com.heartsave.todaktodak_api.common.BaseTestObject.TEST_WEBTOON_URL;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiBgmRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.AiWebtoonRequest;
import com.heartsave.todaktodak_api.ai.webhook.service.AiDiaryService;
import com.heartsave.todaktodak_api.ai.webhook.service.AiWebhookCharacterService;
import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.config.WebConfig;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = AiWebhookController.class,
    excludeFilters = {
      @ComponentScan.Filter(
          type = FilterType.ASSIGNABLE_TYPE,
          classes = {WebConfig.class})
    })
@AutoConfigureMockMvc(addFilters = false)
class AiWebhookControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private AiDiaryService aiDiaryService;
  @MockBean private AiWebhookCharacterService aiWebhookCharacterService;

  MemberEntity member;
  DiaryEntity diary;

  @BeforeEach
  void setup() {
    member = BaseTestObject.createMember();
    diary = BaseTestObject.createDiaryWithMember(member);
  }

  @Nested
  @DisplayName("AI 웹툰 생성 완료시 저장 API 테스트")
  class SaveWebtoonTest {

    @Test
    @DisplayName("정상적인 요청의 경우 204 상태코드 반환")
    void saveWebtoon_ValidRequest_Returns204() throws Exception {
      AiWebtoonRequest request =
          new AiWebtoonRequest(
              member.getId(), diary.getDiaryCreatedTime().toLocalDate(), TEST_WEBTOON_URL);

      doNothing().when(aiDiaryService).saveWebtoon(any(AiWebtoonRequest.class));

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/webtoon")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());

      verify(aiDiaryService, times(1)).saveWebtoon(any(AiWebtoonRequest.class));
    }

    @Test
    @DisplayName("필수 파라미터가 누락된 경우 400 상태코드 반환")
    void saveWebtoon_InvalidRequest_Returns400() throws Exception {
      AiWebtoonRequest request =
          new AiWebtoonRequest(null, diary.getDiaryCreatedTime().toLocalDate(), TEST_WEBTOON_URL);

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/webtoon")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .characterEncoding(StandardCharsets.UTF_8))
          .andExpect(status().isBadRequest());

      verify(aiDiaryService, never()).saveWebtoon(any(AiWebtoonRequest.class));
    }
  }

  @Nested
  @DisplayName("AI BGM 생성 완료시 저장 API 테스트")
  class SaveBgmTest {

    @Test
    @DisplayName("정상적인 요청의 경우 204 상태코드 반환")
    void saveBgm_ValidRequest_Returns204() throws Exception {
      AiBgmRequest request =
          new AiBgmRequest(member.getId(), diary.getDiaryCreatedTime().toLocalDate(), TEST_BGM_URL);

      doNothing().when(aiDiaryService).saveBgm(any(AiBgmRequest.class));

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/bgm")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());

      verify(aiDiaryService, times(1)).saveBgm(any(AiBgmRequest.class));
    }

    @Test
    @DisplayName("필수 파라미터가 누락된 경우 400 상태코드 반환")
    void saveBgm_InvalidRequest_Returns400() throws Exception {
      AiBgmRequest request =
          new AiBgmRequest(null, diary.getDiaryCreatedTime().toLocalDate(), TEST_BGM_URL);

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/bgm")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .characterEncoding(StandardCharsets.UTF_8))
          .andExpect(status().isBadRequest());

      verify(aiDiaryService, never()).saveBgm(any(AiBgmRequest.class));
    }

    @Test
    @DisplayName("BGM URL이 비어있는 경우 400 상태코드 반환")
    void saveBgm_EmptyBgmUrl_Returns400() throws Exception {
      AiBgmRequest request =
          new AiBgmRequest(member.getId(), diary.getDiaryCreatedTime().toLocalDate(), "");

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/bgm")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .characterEncoding(StandardCharsets.UTF_8))
          .andExpect(status().isBadRequest());

      verify(aiDiaryService, never()).saveBgm(any(AiBgmRequest.class));
    }

    @Test
    @DisplayName("날짜 형식이 잘못된 경우 400 상태코드 반환")
    void saveBgm_InvalidDateFormat_Returns400() throws Exception {
      String requestBody =
          """
          {
            "memberId": 1,
            "date": "2024/11/06",
            "bgmUrl": "music-ai/1/2024/11/06/bgm.mp3"
          }
          """;

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/bgm")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestBody)
                  .characterEncoding(StandardCharsets.UTF_8))
          .andExpect(status().isBadRequest());

      verify(aiDiaryService, never()).saveBgm(any(AiBgmRequest.class));
    }
  }
}
