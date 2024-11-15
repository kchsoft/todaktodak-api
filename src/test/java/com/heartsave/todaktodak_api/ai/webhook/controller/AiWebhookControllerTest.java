package com.heartsave.todaktodak_api.ai.webhook.controller;

import static com.heartsave.todaktodak_api.common.BaseTestObject.TEST_BGM_URL;
import static com.heartsave.todaktodak_api.common.BaseTestObject.TEST_WEBTOON_URL;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookBgmCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookCharacterCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.dto.request.WebhookWebtoonCompletionRequest;
import com.heartsave.todaktodak_api.ai.webhook.service.AiDiaryService;
import com.heartsave.todaktodak_api.ai.webhook.service.AiWebhookCharacterService;
import com.heartsave.todaktodak_api.common.BaseTestObject;
import com.heartsave.todaktodak_api.common.config.WebConfig;
import com.heartsave.todaktodak_api.common.converter.InstantConverter;
import com.heartsave.todaktodak_api.common.exception.errorspec.MemberErrorSpec;
import com.heartsave.todaktodak_api.diary.entity.DiaryEntity;
import com.heartsave.todaktodak_api.member.entity.MemberEntity;
import com.heartsave.todaktodak_api.member.exception.MemberNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
      WebhookWebtoonCompletionRequest request =
          new WebhookWebtoonCompletionRequest(
              member.getId(),
              InstantConverter.toLocalDate(diary.getDiaryCreatedTime()),
              TEST_WEBTOON_URL);

      doNothing().when(aiDiaryService).saveWebtoon(any(WebhookWebtoonCompletionRequest.class));

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/webtoon")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());

      verify(aiDiaryService, times(1)).saveWebtoon(any(WebhookWebtoonCompletionRequest.class));
    }

    @Test
    @DisplayName("필수 파라미터가 누락된 경우 400 상태코드 반환")
    void saveWebtoon_InvalidRequest_Returns400() throws Exception {
      WebhookWebtoonCompletionRequest request =
          new WebhookWebtoonCompletionRequest(
              null, InstantConverter.toLocalDate(diary.getDiaryCreatedTime()), TEST_WEBTOON_URL);

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/webtoon")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .characterEncoding(StandardCharsets.UTF_8))
          .andExpect(status().isBadRequest());

      verify(aiDiaryService, never()).saveWebtoon(any(WebhookWebtoonCompletionRequest.class));
    }
  }

  @Nested
  @DisplayName("AI BGM 생성 완료시 저장 API 테스트")
  class SaveBgmTest {

    @Test
    @DisplayName("정상적인 요청의 경우 204 상태코드 반환")
    void saveBgm_ValidRequest_Returns204() throws Exception {
      WebhookBgmCompletionRequest request =
          new WebhookBgmCompletionRequest(
              member.getId(),
              InstantConverter.toLocalDate(diary.getDiaryCreatedTime()),
              TEST_BGM_URL);

      doNothing().when(aiDiaryService).saveBgm(any(WebhookBgmCompletionRequest.class));

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/bgm")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());

      verify(aiDiaryService, times(1)).saveBgm(any(WebhookBgmCompletionRequest.class));
    }

    @Test
    @DisplayName("필수 파라미터가 누락된 경우 400 상태코드 반환")
    void saveBgm_InvalidRequest_Returns400() throws Exception {
      WebhookBgmCompletionRequest request =
          new WebhookBgmCompletionRequest(
              null, InstantConverter.toLocalDate(diary.getDiaryCreatedTime()), TEST_BGM_URL);

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/bgm")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .characterEncoding(StandardCharsets.UTF_8))
          .andExpect(status().isBadRequest());

      verify(aiDiaryService, never()).saveBgm(any(WebhookBgmCompletionRequest.class));
    }

    @Test
    @DisplayName("BGM URL이 비어있는 경우 400 상태코드 반환")
    void saveBgm_EmptyBgmUrl_Returns400() throws Exception {
      WebhookBgmCompletionRequest request =
          new WebhookBgmCompletionRequest(
              member.getId(), InstantConverter.toLocalDate(diary.getDiaryCreatedTime()), "");

      mockMvc
          .perform(
              post("/api/v1/webhook/ai/bgm")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .characterEncoding(StandardCharsets.UTF_8))
          .andExpect(status().isBadRequest());

      verify(aiDiaryService, never()).saveBgm(any(WebhookBgmCompletionRequest.class));
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

      verify(aiDiaryService, never()).saveBgm(any(WebhookBgmCompletionRequest.class));
    }
  }

  @Nested
  @DisplayName("AI 캐릭터 생성 완료시 저장 API 테스트")
  class SaveCharacterTest {
    @Test
    @DisplayName("캐릭터 저장 성공")
    void saveCharacter_success_204Test() throws Exception {
      // given
      WebhookCharacterCompletionRequest request =
          WebhookCharacterCompletionRequest.builder()
              .memberId(member.getId())
              .characterInfo(member.getCharacterInfo())
              .characterStyle(member.getCharacterStyle())
              .characterProfileImageUrl(member.getCharacterImageUrl())
              .seedNum(member.getCharacterSeed())
              .build();

      // when + then
      mockMvc
          .perform(
              post("/api/v1/webhook/ai/character")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .characterEncoding(StandardCharsets.UTF_8))
          .andExpect(status().isNoContent());
      verify(aiWebhookCharacterService, times(1)).saveCharacterAndNotify(request);
    }

    @ParameterizedTest
    @DisplayName("캐릭터 저장 실패 - 유효하지 않은 요청 정보")
    @MethodSource("getCharacterInvalidArguments")
    void saveCharacter_fail_400Test(
        Long memberId,
        String characterInfo,
        String characterStyle,
        Integer seedNum,
        String characterUrl)
        throws Exception {

      // given
      WebhookCharacterCompletionRequest request =
          WebhookCharacterCompletionRequest.builder()
              .memberId(memberId)
              .characterInfo(characterInfo)
              .characterStyle(characterStyle)
              .characterProfileImageUrl(characterUrl)
              .seedNum(seedNum)
              .build();

      // when
      doNothing()
          .when(aiWebhookCharacterService)
          .saveCharacterAndNotify(any(WebhookCharacterCompletionRequest.class));

      // then
      mockMvc
          .perform(
              post("/api/v1/webhook/ai/character")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(new ObjectMapper().writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> getCharacterInvalidArguments() {
      return Stream.of(
          // null
          Arguments.of(null, "{ \"hair\": \"long\" }", "romance", 123, "folder/image"),

          // blank
          Arguments.of(1L, "", "romance", 123, "folder/image", "characterInfo must not be blank"),

          // 음수 시드값
          Arguments.of(1L, "{ \"hair\": \"long\" }", "romance", -1, "folder/image"));
    }

    @Test
    @DisplayName("캐릭터 저장 실패 - 존재하지 않는 회원")
    void saveCharacter_fail_404Test() throws Exception {
      // given
      WebhookCharacterCompletionRequest request =
          WebhookCharacterCompletionRequest.builder()
              .memberId(member.getId())
              .characterInfo(member.getCharacterInfo())
              .characterStyle(member.getCharacterStyle())
              .characterProfileImageUrl(member.getCharacterImageUrl())
              .seedNum(member.getCharacterSeed())
              .build();

      // when
      doThrow(new MemberNotFoundException(MemberErrorSpec.NOT_FOUND, member.getId()))
          .when(aiWebhookCharacterService)
          .saveCharacterAndNotify(any(WebhookCharacterCompletionRequest.class));

      // then
      mockMvc
          .perform(
              post("/api/v1/webhook/ai/character")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .characterEncoding(StandardCharsets.UTF_8))
          .andExpect(status().isNotFound());
      verify(aiWebhookCharacterService, times(1)).saveCharacterAndNotify(request);
    }
  }
}
